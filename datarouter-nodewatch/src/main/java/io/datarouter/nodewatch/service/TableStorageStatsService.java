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
package io.datarouter.nodewatch.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.datarouter.bytes.ByteLength;
import io.datarouter.nodewatch.service.NodewatchTableStatsService.StorageStats;
import io.datarouter.nodewatch.storage.binarydto.storagestats.service.ServiceStorageStatsBinaryDao;
import io.datarouter.nodewatch.storage.binarydto.storagestats.service.ServiceStorageStatsBinaryDto;
import io.datarouter.nodewatch.storage.binarydto.storagestats.table.TableStorageStatsBinaryDto;
import io.datarouter.nodewatch.storage.latesttablecount.LatestTableCount;
import io.datarouter.nodewatch.util.NodewatchDatabaseType;
import io.datarouter.nodewatch.util.TableStorageSizeTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientAndTableNames;
import io.datarouter.storage.node.type.index.ManagedNode;
import io.datarouter.storage.node.type.index.ManagedNodesHolder;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TableStorageStatsService{

	@Inject
	private ServiceStorageStatsBinaryDao serviceStorageStatsBinaryDao;
	@Inject
	private ManagedNodesHolder managedNodesHolder;

	public Map<ClientAndTableNames,TableStorageStatsBinaryDto> loadTableStats(){
		Optional<ServiceStorageStatsBinaryDto> optPersistedStats = serviceStorageStatsBinaryDao.find();
		if(optPersistedStats.isEmpty()){
			return Map.of();
		}
		ServiceStorageStatsBinaryDto persistedStats = optPersistedStats.orElseThrow();
		return Scanner.of(persistedStats.tables)
				.toMap(TableStorageStatsBinaryDto::clientAndTableNames);
	}

	public Optional<StorageStats> findStorageStats(
			PhysicalNode<?,?,?> physicalNode,
			TableStorageStatsBinaryDto tableStats,
			LatestTableCount latestTableCount){
		if(physicalNode == null || tableStats == null || latestTableCount == null){
			return Optional.empty();
		}
		return Optional.of(makeStorageStats(physicalNode, tableStats, latestTableCount.getNumRows()));
	}

	private StorageStats makeStorageStats(
			PhysicalNode<?,?,?> physicalNode,
			TableStorageStatsBinaryDto tableStats,
			long totalRows){
		Optional<NodewatchDatabaseType> optDatabaseType = NodewatchDatabaseType.findPrice(physicalNode.getClientType());

		List<? extends ManagedNode<?,?,?,?,?>> managedNodes = managedNodesHolder.getManagedNodes(physicalNode);
		long totalIndexBytes = TableStorageSizeTool.calcTotalIndexSize(tableStats, managedNodes, totalRows).toBytes();

		if(optDatabaseType.isPresent()){
			NodewatchDatabaseType databaseType = optDatabaseType.orElseThrow();
			long bytesPerRow = tableStats.avgBytesPerRow(databaseType.storesColumnNames);
			long totalPrimaryTableBytes = bytesPerRow * totalRows;
			long totalBytes = totalPrimaryTableBytes + totalIndexBytes;
			long estStorageBytes = (long)(totalBytes * databaseType.storageMultiplier);
			double yearlyStorageCostDollars = ByteLength.ofBytes(estStorageBytes).toTiBDouble()
					* databaseType.dollarsPerTiBPerYear();
			Optional<Double> yearlyNodeCostDollars = databaseType.findYearlyNodeCost(
					ByteLength.ofBytes(estStorageBytes));
			double yearlyTotalCostDollars = yearlyStorageCostDollars + yearlyNodeCostDollars.orElse(0d);
			return new StorageStats(
					tableStats.clientAndTableNames(),
					estStorageBytes,
					Optional.of(yearlyStorageCostDollars),
					yearlyNodeCostDollars,
					Optional.of(yearlyTotalCostDollars));
		}else{
			long bytesPerRow = tableStats.avgValueBytesPerRow();
			long totalPrimaryTableBytes = bytesPerRow * totalRows;
			long totalBytes = totalPrimaryTableBytes + totalIndexBytes;
			return new StorageStats(
					tableStats.clientAndTableNames(),
					totalBytes,
					Optional.empty(),
					Optional.empty(),
					Optional.empty());
		}
	}

}
