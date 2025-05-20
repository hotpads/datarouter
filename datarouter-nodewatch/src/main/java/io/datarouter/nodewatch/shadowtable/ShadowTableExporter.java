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
package io.datarouter.nodewatch.shadowtable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.KvString;
import io.datarouter.nodewatch.shadowtable.config.DatarouterShadowTableExecutors.ShadowTableExportReadExecutor;
import io.datarouter.nodewatch.shadowtable.service.ShadowTableNodeSelectionService;
import io.datarouter.nodewatch.shadowtable.service.ShadowTableRangeCombineService;
import io.datarouter.nodewatch.shadowtable.service.ShadowTableRangeCombineService.RangeCombineRequest;
import io.datarouter.nodewatch.shadowtable.service.ShadowTableRangeExportService;
import io.datarouter.nodewatch.shadowtable.service.ShadowTableRangeExportService.RangeExportRequest;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.storage.client.ClientAndTableNames;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;
import io.datarouter.types.Ulid;
import io.datarouter.util.number.NumberFormatter;
import jakarta.inject.Inject;

public class ShadowTableExporter{
	private static final Logger logger = LoggerFactory.getLogger(ShadowTableExporter.class);

	@Inject
	private ShadowTableExportReadExecutor exportReadExecutor;
	@Inject
	private ShadowTableNodeSelectionService nodeSelectionService;
	@Inject
	private ShadowTableRangeExportService rangeExportService;
	@Inject
	private ShadowTableRangeCombineService rangeCombineService;

	private final Map<ClientAndTableNames,AtomicLong> numRowsByTable = new ConcurrentHashMap<>();
	private final Map<ClientAndTableNames,AtomicLong> numRemainingRangesByTable = new ConcurrentHashMap<>();

	public void export(int jobId, ShadowTableExport export){
		String exportId = Ulid.newValue();
		List<PhysicalSortedStorageReaderNode<?,?,?>> nodes = Scanner.of(nodeSelectionService.listNodesForExport(export))
				//Exporting in alphabetical order, but could customize based on table size or other factors
				.sort(Comparator.comparing(
						PhysicalSortedStorageReaderNode::clientAndTableNames,
						ClientAndTableNames.COMPARE_CLIENT_TABLE))
				.list();
		logger.warn("starting {}", new KvString()
				.add("jobId", jobId, NumberFormatter::addCommas)
				.add("exportId", exportId)
				.add("numNodes", nodes.size(), NumberFormatter::addCommas));

		// Export ranges
		Map<String,AtomicLong> rangeSequenceIdByNodeName = new ConcurrentHashMap<>();
		Scanner.of(nodes)
				.concatIter(node -> {
					List<? extends RangeExportRequest<?,?,?>> requests = rangeExportService
							.makeRangeExportRequests(export, exportId, node);
					numRowsByTable.put(node.clientAndTableNames(), new AtomicLong());
					numRemainingRangesByTable.put(node.clientAndTableNames(), new AtomicLong(requests.size()));
					return requests;
				})
				.parallelUnordered(new Threads(exportReadExecutor, export.resource().databaseExportThreads()))
				.forEach(request -> {
					long numRangeRows = rangeExportService.exportUntypedRangeWithRetries(
							request,
							rangeSequenceIdByNodeName
									.computeIfAbsent(request.node().getName(), _ -> new AtomicLong())
									.incrementAndGet());
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

		// Combine ranges
		Scanner.of(nodes)
				.map(node -> new RangeCombineRequest<>(export, exportId, node))
				.forEach(rangeCombineService::combine);
		// TODO delete (empty) range exportId directory
	}

}
