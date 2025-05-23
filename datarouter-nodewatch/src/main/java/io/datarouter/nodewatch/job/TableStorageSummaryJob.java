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
import io.datarouter.nodewatch.storage.binarydto.storagestats.service.ServiceStorageStatsBinaryDao;
import io.datarouter.nodewatch.storage.binarydto.storagestats.service.ServiceStorageStatsBinaryDto;
import io.datarouter.nodewatch.storage.binarydto.storagestats.table.TableStorageStatsBinaryDao;
import io.datarouter.nodewatch.storage.binarydto.storagestats.table.TableStorageStatsBinaryDto;
import io.datarouter.nodewatch.storage.binarydto.storagestats.table.TableStorageStatsBinaryDto.ColumnStorageStatsBinaryDto;
import io.datarouter.nodewatch.storage.tablecount.TableCount;
import io.datarouter.nodewatch.util.NodewatchDatabaseType;
import io.datarouter.nodewatch.util.TableStorageSizeTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientAndTableNames;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.raw.read.SortedStorageReader.PhysicalSortedStorageReaderNode;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.node.type.index.ManagedNodesHolder;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import jakarta.inject.Inject;

public class TableStorageSummaryJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(TableStorageSummaryJob.class);

	private static final long LIMIT_PER_TABLE = 4_000_000;

	@Inject
	private ServiceName serviceNameSupplier;
	@Inject
	private TableSamplerService tableSamplerService;
	@Inject
	private DatarouterNodes datarouterNodes;
	@Inject
	private ManagedNodesHolder managedNodesHolder;
	@Inject
	private TableStorageStatsBinaryDao tableStorageStatsDao;
	@Inject
	private ServiceStorageStatsBinaryDao serviceStorageStatsDao;
	@Inject
	private ClientTypeStorageStatsBinaryDao clientTypeStatsDao;

	@Override
	public void run(TaskTracker tracker){
		List<PhysicalSortedStorageReaderNode<?,?,?>> nodes = tableSamplerService.scanCountableNodes()
				.list();
		Map<ClientType<?,?>,List<PhysicalSortedStorageReaderNode<?,?,?>>> nodesByClientType = Scanner.of(nodes)
				.groupBy(PhysicalNode::getClientType);

		// Save a summary for each table
		List<TableStorageStatsBinaryDto> tableDtos = Scanner.of(nodes)
				.sort(Comparator.comparing(Node::getName))
				.advanceUntil(_ -> tracker.increment().shouldStop())
				.map(node -> processTable(tracker, node))
				.list();
		// Save a service-level summary
		var serviceDto = new ServiceStorageStatsBinaryDto(serviceNameSupplier.get(), tableDtos);
		serviceStorageStatsDao.write(serviceDto);
		// Save a summary for each ClientType
		nodesByClientType
				.forEach(this::processClientType);
	}

	private TableStorageStatsBinaryDto processTable(
			TaskTracker tracker,
			PhysicalSortedStorageReaderNode<?,?,?> node){
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
		return dto;
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
		tableStorageStatsDao.scanTableSummaryDtos(clientType, nodes).forEach(stats -> {
			TableCount tableCount = tableSamplerService.getCurrentTableCountFromSamples(stats.clientAndTableNames());
			long totalRows = tableCount.getNumRows();
			// primary table bytes
			totalNameBytes.addAndGet(totalRows * stats.avgNameBytesPerRow());
			totalValueBytes.addAndGet(totalRows * stats.avgValueBytesPerRow());
			// secondary index bytes
			PhysicalNode<?,?,?> physicalNode = datarouterNodes.getPhysicalNodeForClientAndTable(
					stats.clientName,
					stats.tableName);
			List<? extends ManagedNode<?,?,?,?,?>> managedNodes = managedNodesHolder.getManagedNodes(physicalNode);
			long totalIndexBytes = TableStorageSizeTool.calcTotalIndexSize(stats, managedNodes, totalRows).toBytes();
			totalValueBytes.addAndGet(totalIndexBytes);
		});
		var dto = new ClientTypeStorageStatsBinaryDto(
				clientType.getName(),
				totalNameBytes.get(),
				totalValueBytes.get());
		clientTypeStatsDao.saveClientTypeDto(clientType, dto);
		logger.warn("saved clientType={}", dto);
	}

}
