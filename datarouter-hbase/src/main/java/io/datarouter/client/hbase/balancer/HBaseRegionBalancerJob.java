/**
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
package io.datarouter.client.hbase.balancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.cluster.DrRegionInfo;
import io.datarouter.client.hbase.cluster.DrRegionListFactory;
import io.datarouter.client.hbase.cluster.DrRegionListFactory.DrRegionList;
import io.datarouter.client.hbase.cluster.DrServerInfo;
import io.datarouter.client.hbase.cluster.DrServerList;
import io.datarouter.client.hbase.compaction.HBaseCompactionInfo;
import io.datarouter.client.hbase.config.DatarouterHBaseSettingRoot;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.concurrent.ThreadTool;
import io.datarouter.util.timer.PhaseTimer;

public class HBaseRegionBalancerJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(HBaseRegionBalancerJob.class);

	//recalculate movements periodically in case active servers have changed
	private static final long RECALCULATE_AFTER_MS = TimeUnit.MINUTES.toMillis(3);

	@Inject
	private DatarouterHBaseSettingRoot hbaseSettings;
	@Inject
	private DatarouterNodes nodes;
	@Inject
	private HBaseBalancerFactory balancerFactory;
	@Inject
	private HBaseCompactionInfo compactionInfo;
	@Inject
	private DrRegionListFactory drRegionListFactory;
	@Inject
	private HBaseClientManager hBaseClientManager;

	@Override
	public void run(TaskTracker tracker){
		logger.warn("starting Balancer " + System.identityHashCode(this));
		for(ClientId clientId : compactionInfo.getManagedClientIds()){
			while(!balanceClient(tracker, clientId)){
				if(tracker.shouldStop()){
					return;
				}
			}
		}
	}

	//return true if completed balancing
	private boolean balanceClient(TaskTracker tracker, ClientId clientId){
		logger.warn("calculating region movements for client {}", clientId.getName());
		Admin admin = hBaseClientManager.getAdmin(clientId);
		List<HBaseRegionMovement> movements = new ArrayList<>();
		DrServerList serverList = new DrServerList(admin);
		List<String> tableNames = nodes.getTableNamesForClient(clientId.getName());
		Collections.sort(tableNames);
		int tableCounter = 0;
		for(String tableName : tableNames){
			if(tracker.shouldStop()){
				return false;
			}
			++tableCounter;
			PhaseTimer timer = new PhaseTimer("generating movements for table " + tableName + " #" + tableCounter + "/"
					+ tableNames.size());
			List<HBaseRegionMovement> tableMovements = new ArrayList<>();
			PhysicalNode<?,?,?> physicalNodeForTable = nodes.getPhysicalNodeForClientAndTable(clientId.getName(),
					tableName);
			BaseHBaseRegionBalancer balancer = balancerFactory.getBalancerForTable(clientId, tableName);
			DrRegionList regionList;
			try{
				regionList = drRegionListFactory.make(clientId, serverList, tableName, physicalNodeForTable, balancer,
						compactionInfo);
			}catch(Exception e){
				logger.error("skipping table " + tableName, e);
				continue;
			}
			for(DrRegionInfo<?> region : regionList.getRegions()){
				if(region.isNotOnAnyServer()){
					logger.warn("region {} is not currently hosted, so not attempting to move it", region.getRegion()
							.getRegionNameAsString());
					continue;
				}
				if(region.isOnCorrectServer()){
					continue;
				}
				HBaseRegionMovement movement = new HBaseRegionMovement(
						tableName,
						region.getRegion().getEncodedName(),
						region.getHBaseServerName(),
						region.getBalancerDestinationHBaseServerName());
				tableMovements.add(movement);
			}
			timer.add("generated " + tableMovements.size() + " movements");
			logger.warn(timer.toString());
			movements.addAll(tableMovements);
		}

		//we calculated all the movements.  now move them
		long iterationStartTimeMs = System.currentTimeMillis();
		Map<ServerName,List<HBaseRegionMovement>> movementsByCurrentServer = HBaseRegionMovement.getByCurrentServer(
				movements);
		int clusterMovementCounter = 0;
		logger.warn("processing {} total movements", movements.size());

		for(DrServerInfo serverInfo : serverList.getServersSortedByDescendingLoad()){
			List<HBaseRegionMovement> movementsForServer = movementsByCurrentServer.get(serverInfo.getServerName());
			logger.warn("expecting {} movements for server {}", CollectionTool.sizeNullSafe(movementsForServer),
					serverInfo.getServerName());
		}
		for(DrServerInfo serverInfo : serverList.getServersSortedByDescendingLoad()){
			List<HBaseRegionMovement> movementsForServer = movementsByCurrentServer.getOrDefault(serverInfo
					.getServerName(), List.of());
			logger.warn("processing {} movements for server {}", CollectionTool.sizeNullSafe(movementsForServer),
					serverInfo.getServerName());
			int serverMovementCounter = 0;
			for(HBaseRegionMovement movement : movementsForServer){
				++clusterMovementCounter;
				++serverMovementCounter;
				logger.warn("moving region {}/{} ({}/{}): {}", serverMovementCounter, movementsForServer.size(),
						clusterMovementCounter, movements.size(), movement);
				try{
					admin.move(movement.getRegionNameBytes(), movement.getDestinationServerNameBytes());
				}catch(Exception ex){//in 0.94, this is UndeclaredThrowableException wrapping other Exceptions
					logger.error("exception moving region, skipping", ex);
				}
				ThreadTool.sleepUnchecked(hbaseSettings.getSleepBetweenRegionMovementMs());
				if(tracker.increment().shouldStop()){
					return false;
				}
				if(System.currentTimeMillis() - iterationStartTimeMs > RECALCULATE_AFTER_MS){
					logger.warn("suspending to check for new servers", RECALCULATE_AFTER_MS);
					return false;
				}
			}
			logger.warn("processed {} movements for server {}", serverMovementCounter, serverInfo.getServerName());
		}
		return true;
	}

}
