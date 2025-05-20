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
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.KvString;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.nodewatch.shadowtable.ShadowTableExport;
import io.datarouter.nodewatch.shadowtable.ShadowTableMetrics;
import io.datarouter.nodewatch.shadowtable.config.DatarouterShadowTableExecutors.ShadowTableCombinePrefetchExecutor;
import io.datarouter.nodewatch.shadowtable.config.DatarouterShadowTableSettingRoot;
import io.datarouter.nodewatch.shadowtable.storage.ShadowTableBlockfileDao;
import io.datarouter.nodewatch.shadowtable.storage.ShadowTableRangeBlockfileDao;
import io.datarouter.scanner.BatchByMinSizeScanner.ScannerMinSizeBatch;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;
import io.datarouter.util.Count.Counts;
import io.datarouter.util.number.NumberFormatter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ShadowTableRangeCombineService{
	private static final Logger logger = LoggerFactory.getLogger(ShadowTableRangeCombineService.class);

	private static final int MAX_CONSIDERED_VCPUS = 16;
	private static final ByteLength BLOB_PREFETCHER_BUFFER_SIZE_PER_VCPU = ByteLength.ofMiB(256);// 4 GiB max
	private static final ByteLength PREFETCH_GROUP_SIZE = ByteLength.ofMiB(16);
	private static final int PREFETCH_GROUPS_PER_VCPU = 4;// 64 MiB per vcpu, 1 GiB max
	private static final ByteLength OUTPUT_BLOCK_SIZE = ByteLength.ofKiB(32);

	@Inject
	private DatarouterShadowTableSettingRoot settings;
	@Inject
	private ShadowTableNodeSelectionService nodeSelectionService;
	@Inject
	private ShadowTableRangeBlockfileDao rangeBlockfileDao;
	@Inject
	private ShadowTableBlockfileDao blockfileDao;
	@Inject
	private ShadowTableCombinePrefetchExecutor combinePrefetchExec;

	public record RangeCombineRequest<
					PK extends PrimaryKey<PK>,
					D extends Databean<PK,D>,
					F extends DatabeanFielder<PK,D>>(
			ShadowTableExport export,
			String exportId,
			PhysicalSortedStorageReaderNode<PK,D,F> node){
	}

	public <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	void combine(RangeCombineRequest<PK,D,F> request){
		long numRanges = rangeBlockfileDao.numFiles(
				request.export(),
				request.exportId(),
				request.node().clientAndTableNames().table());
		int effectiveNumVcpus = Math.min(
				request.export().resource().vcpus(),
				MAX_CONSIDERED_VCPUS);
		ByteLength blobPrefetcherBufferSize = ByteLength.ofBytes(
				BLOB_PREFETCHER_BUFFER_SIZE_PER_VCPU.toBytes() * effectiveNumVcpus);
		int numPrefetchGroups = PREFETCH_GROUPS_PER_VCPU * effectiveNumVcpus;

		var counts = new Counts();
		var rowCount = counts.add("rows");
		var waitOnReadNs = counts.add("waitOnReadNs");
		var prefetchedBlockTally = counts.add("prefetchedBlockTally");
		Supplier<KvString> logKvSupplier = () -> new KvString()
				.add("client", request.node().clientAndTableNames().client())
				.add("table", request.node().clientAndTableNames().table())
				.add("rows", rowCount.valueToString())
				.add("readStallMs", Duration.ofNanos(waitOnReadNs.value()).toMillis(), NumberFormatter::addCommas)
				.add("prefetchedBlocks", prefetchedBlockTally.valueToString());

		logger.warn("startCombineRanges {}", logKvSupplier.get()
				.add("totalRanges", numRanges, NumberFormatter::addCommas));

		Scanner<BlockfileRow> inputRows = settings.useBlobPrefetcher.get()
				? rangeBlockfileDao.scanConcatenatedRangeRowsWithBlobPrefetcher(
						request.export(),
						request.exportId(),
						request.node().clientAndTableNames().table(),
						blobPrefetcherBufferSize)
				: rangeBlockfileDao.scanConcatenatedRangeRows(
						request.export(),
						request.exportId(),
						request.node().clientAndTableNames().table());
		Scanner<List<BlockfileRow>> rowBlocks = inputRows
				// Group rows into blocks.
				.batchByMinSizeWithStats(OUTPUT_BLOCK_SIZE.toBytes(), BlockfileRow::length)
				.map(BlockOfRowsWithStats::new)
				// Batch blocks into a bigger "prefetch group".
				// Based on memory usage of the BlockfileRow data plus object overhead.
				.batchByMinSize(PREFETCH_GROUP_SIZE.toBytes(), BlockOfRowsWithStats::totalBytes)
				// Monitoring.
				.each(prefetchGroup -> {
					long numRows = prefetchGroup.stream()
							.map(BlockOfRowsWithStats::rows)
							.mapToLong(List::size)
							.sum();
					long numBytes = prefetchGroup.stream()
							.mapToLong(BlockOfRowsWithStats::totalBytes)
							.sum();
					long numBlocks = prefetchGroup.size();
					rowCount.incrementBy(numRows);
					ShadowTableMetrics.countCombineRows(
							request.export.name(),
							request.node.clientAndTableNames(),
							numRows);
					ShadowTableMetrics.countCombineBytesIn(
							request.export.name(),
							request.node.clientAndTableNames(),
							numBytes);
					ShadowTableMetrics.countCombineBlocksOut(
							request.export.name(),
							request.node.clientAndTableNames(),
							numBlocks);
					ShadowTableMetrics.measureCombineBlocksPrefetched(
							request.export.name(),
							request.node.clientAndTableNames(),
							prefetchedBlockTally.value());
				})
				.each(prefetchGroup -> prefetchedBlockTally.incrementBy(prefetchGroup.size()))
				// Prefetch some number of groups based on available memory.
				.prefetch(combinePrefetchExec, numPrefetchGroups)
				.each(prefetchGroup -> prefetchedBlockTally.decrementBy(prefetchGroup.size()))
				.timeNanos(waitOnReadNs::incrementBy)
				.periodic(Duration.ofSeconds(5), _ -> logger.warn("combineRanges {}", logKvSupplier.get()))
				.concat(Scanner::of)
				.map(BlockOfRowsWithStats::rows);

		blockfileDao.writeBlockfile(
				request.export(),
				request.exportId(),
				request.node(),
				nodeSelectionService.enableCompression(request.node),
				rowBlocks);
		rangeBlockfileDao.deleteRangeFiles(
				request.export(),
				request.exportId(),
				request.node().clientAndTableNames().table());
		logger.warn("finishCombineRanges {}", logKvSupplier.get());
	}

	private record BlockOfRowsWithStats(
			List<BlockfileRow> rows,
			long totalBytes){

		private BlockOfRowsWithStats(ScannerMinSizeBatch<BlockfileRow> rows){
			this(rows.items(), totalBytes(rows));
		}

		private static final long totalBytes(ScannerMinSizeBatch<BlockfileRow> batch){
			long dataBytes = batch.totalSize();
			long overheadPerRow = 8 + BlockfileRow.MEMORY_OVERHEAD_BYTES;// 8 is for the List reference
			long overheadBytes = overheadPerRow * batch.items().size();
			return dataBytes + overheadBytes;
		}

	}

}
