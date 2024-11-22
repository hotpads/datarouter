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
package io.datarouter.nodewatch.shadowtable.job;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteLength;
import io.datarouter.bytes.KvString;
import io.datarouter.bytes.blockfile.row.BlockfileRow;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.shadowtable.ShadowTableConfig;
import io.datarouter.nodewatch.shadowtable.ShadowTableExport;
import io.datarouter.nodewatch.shadowtable.ShadowTableMetrics;
import io.datarouter.nodewatch.shadowtable.codec.ShadowTableDictionaryCodec;
import io.datarouter.nodewatch.shadowtable.codec.ShadowTableDictionaryCodec.ColumnNameCodec;
import io.datarouter.nodewatch.shadowtable.config.DatarouterShadowTableExecutors.ShadowTableExportReadExecutor;
import io.datarouter.nodewatch.shadowtable.config.DatarouterShadowTableSettingRoot;
import io.datarouter.nodewatch.shadowtable.config.DatarouterShadowTableTriggerGroup.ShadowTableJobId;
import io.datarouter.nodewatch.shadowtable.service.ShadowTableNodeSelectionService;
import io.datarouter.nodewatch.shadowtable.storage.ShadowTableRangeBlockfileDao;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.client.ClientAndTableNames;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;
import io.datarouter.types.Ulid;
import io.datarouter.util.Require;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.tuple.Range;
import jakarta.inject.Inject;

public abstract class BaseShadowTableExportJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(BaseShadowTableExportJob.class);

	private static final Duration SLOW_RANGE_THRESHOLD = Duration.ofMinutes(1);

	@Inject
	private DatarouterShadowTableSettingRoot settingRoot;
	@Inject
	private ShadowTableConfig shadowTableConfig;
	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private ShadowTableExportReadExecutor exportReadExecutor;
	@Inject
	private ShadowTableNodeSelectionService nodeSelectionService;
	@Inject
	private ShadowTableRangeBlockfileDao rangeBlockfileDao;

	private final Map<ClientAndTableNames,AtomicLong> numRowsByTable = new ConcurrentHashMap<>();
	private final Map<ClientAndTableNames,AtomicLong> numRemainingRangesByTable = new ConcurrentHashMap<>();

	@Override
	public void run(TaskTracker tracker){
		int jobId = ShadowTableJobId.toIndex(this);
		ShadowTableExport export = shadowTableConfig.exportWithIndex(jobId);
		List<PhysicalSortedStorageReaderNode<?,?,?>> nodes = nodeSelectionService.listNodesForExport(export);
		String exportId = Ulid.newValue();
		logger.warn("starting {}", new KvString()
				.add("jobId", jobId, NumberFormatter::addCommas)
				.add("exportId", exportId)
				.add("numNodes", nodes.size(), NumberFormatter::addCommas));
		Scanner.of(nodes)
				//Exporting in alphabetical order, but could customize based on table size or other factors
				.sort(Comparator.comparing(
						PhysicalSortedStorageReaderNode::clientAndTableNames,
						ClientAndTableNames.COMPARE_CLIENT_TABLE))
				.concatIter(node -> {
					var requests = makeRangeExportRequests(export, exportId, node);
					numRowsByTable.put(node.clientAndTableNames(), new AtomicLong());
					numRemainingRangesByTable.put(node.clientAndTableNames(), new AtomicLong(requests.size()));
					return requests;
				})
				.parallelUnordered(new Threads(exportReadExecutor, export.resource().threads()))
				.forEach(request -> {
					long numRangeRows = exportUntypedRangeWithRetries(request);
					long numTableRows = numRowsByTable
							.get(request.node().clientAndTableNames())
							.addAndGet(numRangeRows);
					long numRemainingRangesForTable = numRemainingRangesByTable
							.get(request.node().clientAndTableNames())
							.decrementAndGet();
					if(numRemainingRangesForTable == 0){
						logger.warn("exportedTable {}", new KvString()
								.add("client", request.node().clientAndTableNames().client())
								.add("table", request.node().clientAndTableNames().table())
								.add("ranges", request.numRanges(), NumberFormatter::addCommas)
								.add("rows", numTableRows, NumberFormatter::addCommas));
						numRowsByTable.remove(request.node().clientAndTableNames());
						numRemainingRangesByTable.remove(request.node().clientAndTableNames());
					}
				});
	}

	/*------- make RangeExportRequests -------*/

	private record RangeExportRequest<
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

	private <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	List<RangeExportRequest<PK,D,F>> makeRangeExportRequests(
			ShadowTableExport export,
			String exportId,
			PhysicalSortedStorageReaderNode<PK,D,F> node){
		// Gather all the ranges quickly to minimize the chance of the table sampler changing them while running.
		// Shuffle them to avoid hotspotting on distributed databases.
		List<Range<PK>> ranges = tableSamplerService.scanTableRangesUsingTableSamples(node).list();
		var rangeIdOneBased = new AtomicInteger();
		return Scanner.of(ranges)
				.shuffle()
				.map(range -> new RangeExportRequest<>(
						export,
						exportId,
						node,
						range,
						rangeIdOneBased.incrementAndGet(),
						ranges.size()))
				.list();
	}

	/*------- execute RangeExportRequests -------*/

	private <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	long exportUntypedRangeWithRetries(RangeExportRequest<?,?,?> untypedRequest){
		@SuppressWarnings("unchecked")
		RangeExportRequest<PK,D,F> request = (RangeExportRequest<PK,D,F>)untypedRequest;
		return exportRangeWithRetries(request);
	}

	private <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	long exportRangeWithRetries(RangeExportRequest<PK,D,F> request){
		int attempts = 3;
		Supplier<KvString> logKvSupplier = () -> new KvString()
				.add("client", request.node.clientAndTableNames().client())
				.add("table", request.node.clientAndTableNames().table())
				.add("id", NumberFormatter.addCommas(request.rangeIdOneBased)
						+ "/" + NumberFormatter.addCommas(request.numRanges));
		RuntimeException exception = null;
		for(int attempt = 1; attempt <= attempts; ++attempt){
			try{
				return exportRange(request, attempt);
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
			int attempt){
		Instant startTime = Instant.now();
		Supplier<Duration> durationSupplier = () -> Duration.between(startTime, Instant.now());
		ColumnNameCodec columnNameCodec = ColumnNameCodec.createNewMappings(request.node.getFieldInfo());
		var codec = new ShadowTableDictionaryCodec<>(request.node.getFieldInfo(), columnNameCodec);
		boolean enableCompression = nodeSelectionService.enableCompression(request.node);
		int scanBatchSize = nodeSelectionService.batchSizeForNode(request.node);
		var config = new Config().setResponseBatchSize(scanBatchSize);
		var totalRows = new AtomicLong();
		var totalDatabeanBytes = new AtomicLong();
		var totalBlockfileInputBytes = new AtomicLong();
		var totalBlockfileOutputBytes = new AtomicLong();
		Supplier<KvString> logKvSupplier = () -> new KvString()
				.add("client", request.node.clientAndTableNames().client())
				.add("table", request.node.clientAndTableNames().table())
				.add("id", NumberFormatter.addCommas(request.rangeIdOneBased)
						+ "/" + NumberFormatter.addCommas(request.numRanges))
				.add("rows", totalRows.get(), NumberFormatter::addCommas)
				.add("databeanBytes", ByteLength.ofBytes(totalDatabeanBytes.get()).toDisplay())
				.add("blockfileInputBytes", ByteLength.ofBytes(totalBlockfileInputBytes.get()).toDisplay())
				.add("blockfileOutputBytes", ByteLength.ofBytes(totalBlockfileOutputBytes.get()).toDisplay())
				.add("duration", new DatarouterDuration(durationSupplier.get()).toString())
				.add("attempt", attempt, NumberFormatter::addCommas)
				.add("range", request.range, Range::toString);

		request.node.scan(request.range, config)
				// Batch for efficient monitoring.
				.batch(scanBatchSize)
				// Interrupt the job if the cluster setting is disabled.
				.each($ -> Require.isTrue(settingRoot.runExports.get(), "Export setting was disabled"))
				// Monitor row counts.
				.each(batch -> {
					long numRows = batch.size();
					totalRows.addAndGet(numRows);
					ShadowTableMetrics.countExportRows(
							request.export.name(),
							request.node.clientAndTableNames(),
							numRows);
				})
				// Monitor byte counts (server network ingress).
				// Disabling because it should be close to blockfileInputBytes when using ShadowTableDictionaryCodec.
//				.each(batch -> {
//					long numDatabeanBytes = codec.approximateDatabeanValueBytes(batch);
//					totalDatabeanBytes.addAndGet(numDatabeanBytes);
//					ShadowTableMetrics.countDatabeanBytes(
//							request.export.name(),
//							request.node.clientAndTableNames(),
//							numDatabeanBytes);
//				})
				// Log slow ranges.
				.periodic(
						SLOW_RANGE_THRESHOLD,
						$ -> logger.warn("slowRange {}", logKvSupplier.get()))
				// Flatten after monitoring.
				.concat(Scanner::of)
				.map(codec::encode)
				// Split rows into blocks.
				.batchByMinSize(ShadowTableRangeBlockfileDao.BLOCK_SIZE.toBytes(), BlockfileRow::length)
				// Monitor blockfile input bytes (pre-compressed data).
				.each(batch -> {
					long numBlockfileInputBytes = BlockfileRow.totalLength(batch);
					totalBlockfileInputBytes.addAndGet(numBlockfileInputBytes);
					ShadowTableMetrics.countBlockfileInputBytes(
							request.export.name(),
							request.node.clientAndTableNames(),
							numBlockfileInputBytes);
				})
				// Write blocks of rows into blockfile.
				.then(blocksOfRows -> rangeBlockfileDao.writeBlockfile(
						request.export,
						request.exportId,
						request.node,
						enableCompression,
						request.rangeIdOneBased,
						request.numRanges,
						blocksOfRows));
		logger.warn("exportedRange {}", logKvSupplier.get());
		return totalRows.get();
	}

}
