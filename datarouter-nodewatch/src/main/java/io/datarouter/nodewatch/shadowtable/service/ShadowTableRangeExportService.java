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
package io.datarouter.nodewatch.shadowtable.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.KvString;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.shadowtable.ShadowTableExport;
import io.datarouter.nodewatch.shadowtable.ShadowTableMetrics;
import io.datarouter.nodewatch.shadowtable.codec.ShadowTableStatefulDictionaryCodec;
import io.datarouter.nodewatch.shadowtable.codec.ShadowTableStatefulDictionaryCodec.ColumnNameCodec;
import io.datarouter.nodewatch.shadowtable.config.DatarouterShadowTableSettingRoot;
import io.datarouter.nodewatch.shadowtable.storage.ShadowTableRangeBlockfileDao;
import io.datarouter.scanner.BatchByMinSizeScanner.ScannerMinSizeBatch;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;
import io.datarouter.util.Require;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.tuple.Range;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ShadowTableRangeExportService{
	private static final Logger logger = LoggerFactory.getLogger(ShadowTableRangeExportService.class);

	private static final Duration GROUP_SAMPLES_BY_COUNTING_TIME = Duration.ofSeconds(10);
	private static final Duration SLOW_RANGE_THRESHOLD = Duration.ofMinutes(2);

	@Inject
	private DatarouterShadowTableSettingRoot settingRoot;
	@Inject
	private ShadowTableNodeSelectionService nodeSelectionService;
	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private ShadowTableRangeBlockfileDao rangeBlockfileDao;

	public record RangeExportRequest<
					PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					F extends DatabeanFielder<PK,D>>(
			ShadowTableExport export,
			String exportId,
			PhysicalSortedStorageReaderNode<PK,D,F> node,
			Range<PK> range,
			int rangeIdOneBased,
			int numRanges){
	}

	public <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	List<RangeExportRequest<PK,D,F>> makeRangeExportRequests(
			ShadowTableExport export,
			String exportId,
			PhysicalSortedStorageReaderNode<PK,D,F> node){
		// Gather all the ranges quickly to minimize the chance of the table sampler changing them while running.
		// Group small ranges together based on the countTimeMs stored in the samples
		// Shuffle them to avoid hotspotting on distributed databases.
		List<Range<PK>> ranges = tableSamplerService
				.scanSampledPkRangesByCountingTime(node, GROUP_SAMPLES_BY_COUNTING_TIME)
				.list();
		var rangeIdOneBased = new AtomicInteger();
		return Scanner.of(ranges)
				.map(range -> new RangeExportRequest<>(
						export,
						exportId,
						node,
						range,
						rangeIdOneBased.incrementAndGet(),
						ranges.size()))
				.shuffle()
				.list();
	}

	public <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	long exportUntypedRangeWithRetries(RangeExportRequest<?,?,?> untypedRequest, long sequenceId){
		@SuppressWarnings("unchecked")
		RangeExportRequest<PK,D,F> request = (RangeExportRequest<PK,D,F>)untypedRequest;
		return exportRangeWithRetries(request, sequenceId);
	}

	private <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	long exportRangeWithRetries(RangeExportRequest<PK,D,F> request, long sequenceId){
		int attempts = 3;
		Supplier<KvString> logKvSupplier = () -> new KvString()
				.add("client", request.node.clientAndTableNames().client())
				.add("table", request.node.clientAndTableNames().table())
				.add("sequenceId", NumberFormatter.addCommas(sequenceId)
						+ "/" + NumberFormatter.addCommas(request.numRanges))
				.add("id", NumberFormatter.addCommas(request.rangeIdOneBased));
		RuntimeException exception = null;
		for(int attempt = 1; attempt <= attempts; ++attempt){
			try{
				return exportRange(request, sequenceId, attempt);
			}catch(RuntimeException e){
				exception = e;
				String message = "RangeFailure " + logKvSupplier.get()
						.add("attempt", "%s/%s".formatted(attempt, attempts));
				logger.warn(message, e);
			}
		}
		String message = "Failure " + logKvSupplier.get()
				.add("attempts", attempts, NumberFormatter::addCommas);
		throw new RuntimeException(message, exception);
	}

	private <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	long exportRange(
			RangeExportRequest<PK,D,F> request,
			long sequenceId,
			int attempt){
		Instant startTime = Instant.now();
		Supplier<Duration> durationSupplier = () -> Duration.between(startTime, Instant.now());
		ColumnNameCodec columnNameCodec = ColumnNameCodec.createNewMappings(request.node.getFieldInfo());
		var codec = new ShadowTableStatefulDictionaryCodec<>(request.node.getFieldInfo(), columnNameCodec);
		boolean enableRangeCompression = nodeSelectionService.enableCompression(request.node);
				// Try disabling compression if we're going to delete the ranges after combining them.
//				&& !request.export().combineRanges();
		int scanBatchSize = nodeSelectionService.scanBatchSizeForNode(request.node);
		var config = new Config().setResponseBatchSize(scanBatchSize);
		var totalRows = new AtomicLong();
		var totalBlockfileRwBytes = new AtomicLong();
		Supplier<KvString> logKvSupplier = () -> new KvString()
				.add("client", request.node.clientAndTableNames().client())
				.add("table", request.node.clientAndTableNames().table())
				.add("sequenceId", NumberFormatter.addCommas(sequenceId)
						+ "/" + NumberFormatter.addCommas(request.numRanges))
				.add("id", NumberFormatter.addCommas(request.rangeIdOneBased))
				.add("rows", totalRows.get(), NumberFormatter::addCommas)
				.add("blockfileRowBytes", ByteLength.ofBytes(totalBlockfileRwBytes.get()).toDisplay())
				.add("duration", new DatarouterDuration(durationSupplier.get()).toString())
				.add("attempt", attempt, NumberFormatter::addCommas)
				.add("range", request.range, Range::toString);

		request.node.scan(request.range, config)
				.map(codec::encode)
				// Split rows into blocks.
				.batchByMinSizeWithStats(ShadowTableRangeBlockfileDao.BLOCK_SIZE.toBytes(), BlockfileRow::length)
				.each(batch -> {
					// Interrupt the job if the cluster setting is disabled.
					Require.isTrue(settingRoot.runExports.get(), "Export setting was disabled");
					// Monitor row counts
					totalRows.addAndGet(batch.items().size());
					ShadowTableMetrics.countExportRows(
							request.export.name(),
							request.node.clientAndTableNames(),
							batch.items().size());
					// Monitor byte counts (uncompressed data)
					totalBlockfileRwBytes.addAndGet(batch.totalSize());
					ShadowTableMetrics.countBlockfileInputBytes(
							request.export.name(),
							request.node.clientAndTableNames(),
							batch.totalSize());
				})
				// Log slow ranges.
				.periodic(
						SLOW_RANGE_THRESHOLD,
						_ -> logger.warn("slowRange {}", logKvSupplier.get()))
				.map(ScannerMinSizeBatch::items)
				// Write blocks of rows into blockfile.
				.then(blocksOfRows -> rangeBlockfileDao.writeBlockfile(
						request.export,
						request.exportId,
						request.node,
						enableRangeCompression,
						request.rangeIdOneBased,
						request.numRanges,
						blocksOfRows));
		logger.info("exportedRange {}", logKvSupplier.get());
		return totalRows.get();
	}

}
