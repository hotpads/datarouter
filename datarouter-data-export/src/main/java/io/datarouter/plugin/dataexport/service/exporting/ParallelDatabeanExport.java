/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.plugin.dataexport.service.exporting;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.kvfile.io.write.KvFileWriter;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.plugin.dataexport.service.exporting.DatabeanExportService.DatabeanExportRequest;
import io.datarouter.plugin.dataexport.service.exporting.DatabeanExportService.DatabeanExportResponse;
import io.datarouter.plugin.dataexport.service.exporting.DatabeanExportTracker.Nested.DatabeanExportTrackerType;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.util.BlockfileDirectoryStorage;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.tuple.Range;

public class ParallelDatabeanExport<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>{
	private static final Logger logger = LoggerFactory.getLogger(ParallelDatabeanExport.class);

	private static final int DATABEANS_PER_BLOCK = 1_000;
	private static final Duration TABLE_LOG_PERIOD = Duration.ofSeconds(5);
	private static final Duration PART_LOG_PERIOD = Duration.ofSeconds(10);
	private static final Duration PART_LOG_DELAY = Duration.ofSeconds(30);

	private final DatabeanExportRequest<PK,D,F> request;
	private final DatabeanExportTracker tableTracker;

	public ParallelDatabeanExport(DatabeanExportRequest<PK,D,F> request){
		this.request = request;
		tableTracker = new DatabeanExportTracker(
				DatabeanExportTrackerType.TABLE,
				request.exportId(),
				request.node().getClientId().getName(),
				request.node().getFieldInfo().getTableName(),
				request.partThreads().count(),
				Duration.ZERO);
	}

	/**
	 * Note that partIds should be kept ordered according to the nodewatch samples.
	 */
	record PartIdAndRanges<
					PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					F extends DatabeanFielder<PK,D>>(
			int partId,
			List<Range<PK>> ranges){
	}
	public DatabeanExportResponse exportTable(){
		var scanConfig = new Config().setRequestBatchSize(request.scanBatchSize());
		List<PartIdAndRanges<PK,D,F>> parts = scanRanges(true).list();
		logger.warn("starting numParts={}", parts.size() + 1);
		Scanner.of(parts)
				.parallelUnordered(request.partThreads())
				.each(part -> exportPart(
						part.partId(),
						//TODO should probably merge the ranges into a single range/scan
						Scanner.of(part.ranges())
								.concat(range -> request.node().scan(range, scanConfig))))
				.periodic(TABLE_LOG_PERIOD, $ -> tableTracker.logProgress())
				.count();
		tableTracker.logProgress();
		return new DatabeanExportResponse(
				request.node().getName(),
				parts.size(),
				tableTracker.databeanCount().value());
	}

	public Scanner<PartIdAndRanges<PK,D,F>> scanRanges(boolean smartOrder){
		var partId = new AtomicInteger();
		Scanner<PartIdAndRanges<PK,D,F>> partScanner = request.tableSamplerService()
				.scanTableRangesUsingTableSamples(request.node())
				.include(range -> request.pkRange().contains(range.getStart())
						|| request.pkRange().contains(range.getEnd()))
				.batch(request.tableSamplesPerPart())
				// The partIds need to remain ordered with the samples.
				.map(ranges -> new PartIdAndRanges<>(partId.getAndIncrement(), ranges));
		if(!smartOrder){
			return partScanner;
		}
		// Gather a List of Ranges that is fixed in time.
		// Reduces the chance of the keys shifting under us while exporting.
		// This may require a lot of memory for tables with a huge number of spans.
		// Pre-loading could be optional since the file format doesn't need to know the part count up front.
		List<PartIdAndRanges<PK,D,F>> parts = partScanner.list();
		tableTracker.totalParts().set(parts.size());
		if(parts.size() <= 2){
			return Scanner.of(parts)
					.reverse();
		}
		// Rebuild the list, starting with the last span, then the first span, then the middle spans shuffled.
		List<PartIdAndRanges<PK,D,F>> reorderedParts = new ArrayList<>();
		reorderedParts.add(ListTool.getLast(parts)); //last range
		reorderedParts.add(parts.get(0));// first range
		Scanner.of(parts)
				.skip(1)
				.limit(parts.size() - 2)// without first/last
				.shuffle()
				.forEach(reorderedParts::add);
		return Scanner.of(reorderedParts);
	}

	public void exportPart(int partId, Scanner<D> scanner){
		DatabeanExportTracker partTracker = new DatabeanExportTracker(
				DatabeanExportTrackerType.PART,
				request.exportId(),
				request.node().getClientId().getName(),
				request.node().getFieldInfo().getTableName(),
				1,
				PART_LOG_DELAY);
		partTracker.activePartIds().add(partId);
		tableTracker.activePartIds().add(partId);
		var blockfileStorage = new BlockfileDirectoryStorage(request.tableDirectory());
		KvFileWriter<D> kvFileWriter = request.kvFileService().makeKvFileWriter(
				blockfileStorage,
				request.node(),
				partId);
		scanner
				.include(databean -> request.pkRange().contains(databean.getKey()))
				.batch(DATABEANS_PER_BLOCK)
				.each(batch -> {
					tableTracker.databeanCount().incrementBySize(batch);
					tableTracker.rateTracker().incrementBySize(batch);
					partTracker.databeanCount().incrementBySize(batch);
					partTracker.rateTracker().incrementBySize(batch);
					partTracker.lastKey().set(ListTool.getLast(batch).getKey());
				})
				.periodic(PART_LOG_PERIOD, $ -> partTracker.logProgress())
				.apply(kvFileWriter::write);
		partTracker.logProgress();
		tableTracker.activePartIds().remove(partId);
		tableTracker.completedPartCount().incrementAndGet();
	}

}
