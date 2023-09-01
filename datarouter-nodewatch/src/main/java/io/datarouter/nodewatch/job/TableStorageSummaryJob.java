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
package io.datarouter.nodewatch.job;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.nodewatch.service.TableSamplerService;
import io.datarouter.nodewatch.service.TableStorageSummarizer;
import io.datarouter.nodewatch.service.TableStorageSummarizerDtos.TableSummary;
import io.datarouter.nodewatch.storage.binarydto.storagestats.clienttype.ClientTypeStorageStatsBinaryDao;
import io.datarouter.nodewatch.storage.binarydto.storagestats.clienttype.ClientTypeStorageStatsBinaryDto;
import io.datarouter.nodewatch.storage.binarydto.storagestats.table.TableStorageStatsBinaryDao;
import io.datarouter.nodewatch.storage.binarydto.storagestats.table.TableStorageStatsBinaryDto;
import io.datarouter.nodewatch.storage.binarydto.storagestats.table.TableStorageStatsBinaryDto.ColumnStorageStatsBinaryDto;
import io.datarouter.nodewatch.storage.tablecount.TableCount;
import io.datarouter.nodewatch.util.NodewatchDatabaseType;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientAndTableNames;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import jakarta.inject.Inject;

public class TableStorageSummaryJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(TableStorageSummaryJob.class);

	private static final long LIMIT_PER_TABLE = 4_000_000;

	private final TableSamplerService tableSamplerService;
	private final DatarouterNodes datarouterNodes;
	private final TableStorageStatsBinaryDao tableStorageStatsDao;
	private final ClientTypeStorageStatsBinaryDao clientTypeStatsDao;
	private final List<PhysicalSortedStorageReaderNode<?,?,?>> nodes;
	private final Map<ClientType<?,?>,List<PhysicalSortedStorageReaderNode<?,?,?>>> nodesByClientType;

	@Inject
	public TableStorageSummaryJob(
			TableSamplerService tableSamplerService,
			DatarouterNodes datarouterNodes,
			TableStorageStatsBinaryDao tableStatsDao,
			ClientTypeStorageStatsBinaryDao clientTypeStatsDao){
		this.tableSamplerService = tableSamplerService;
		this.datarouterNodes = datarouterNodes;
		this.tableStorageStatsDao = tableStatsDao;
		this.clientTypeStatsDao = clientTypeStatsDao;
		nodes = tableSamplerService.scanCountableNodes()
				.list();
		nodesByClientType = Scanner.of(nodes)
				.groupBy(PhysicalNode::getClientType);
	}

	@Override
	public void run(TaskTracker tracker){
		// Save a summary for each table
		Scanner.of(nodes)
				.sort(Comparator.comparing(Node::getName))
				.advanceUntil($ -> tracker.increment().shouldStop())
				.forEach(node -> processTable(tracker, node));
		// Save a summary for each ClientType
		nodesByClientType
				.forEach(this::processClientType);
	}

	private void processTable(TaskTracker tracker, PhysicalSortedStorageReaderNode<?,?,?> node){
		ClientAndTableNames clientAndTableNames = node.clientAndTableNames();
		TableSummary tableSummary = new TableStorageSummarizer<>(
				tracker::shouldStop,
				tableSamplerService,
				datarouterNodes,
				clientAndTableNames,
				LIMIT_PER_TABLE)
				.summarizeTable();
		List<ColumnStorageStatsBinaryDto> columnStats = Scanner.of(tableSummary.columnSummaries())
				.map(columnSummary -> new ColumnStorageStatsBinaryDto(
						columnSummary.name(),
						columnSummary.size().avgNameBytes().toBytes(),
						columnSummary.size().avgValueBytes().toBytes()))
				.list();
		var dto = new TableStorageStatsBinaryDto(
				clientAndTableNames.client(),
				clientAndTableNames.table(),
				tableSummary.numRowsIncluded(),
				columnStats);
		tableStorageStatsDao.saveTableDto(node, dto);
		logger.warn("saved table={}", dto);
	}

	private void processClientType(
			ClientType<?,?> clientType,
			List<PhysicalSortedStorageReaderNode<?,?,?>> nodes){
		if(NodewatchDatabaseType.findPrice(clientType).isEmpty()){
			logger.warn("Skipping unknown clientType={}", clientType.getName());
			return;
		}
		var totalNameBytes = new AtomicLong();
		var totalValueBytes = new AtomicLong();
		tableStorageStatsDao.scanTableSummaryDtos(clientType, nodes).forEach(dto -> {
			TableCount tableCount = tableSamplerService.getCurrentTableCountFromSamples(dto.clientAndTableNames());
			long numRows = tableCount.getNumRows();
			totalNameBytes.addAndGet(numRows * dto.avgNameBytesPerRow());
			totalValueBytes.addAndGet(numRows * dto.avgValueBytesPerRow());
		});
		var dto = new ClientTypeStorageStatsBinaryDto(
				clientType.getName(),
				totalNameBytes.get(),
				totalValueBytes.get());
		clientTypeStatsDao.saveClientTypeDto(clientType, dto);
		logger.warn("saved clientType={}", dto);
	}

}
