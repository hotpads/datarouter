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
package io.datarouter.client.hbase.compaction;

import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import javax.inject.Inject;

import org.apache.hadoop.hbase.client.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.balancer.HBaseBalancerFactory;
import io.datarouter.client.hbase.cluster.DrRegionInfo;
import io.datarouter.client.hbase.cluster.DrRegionListFactory;
import io.datarouter.client.hbase.cluster.DrRegionListFactory.DrRegionList;
import io.datarouter.client.hbase.cluster.DrServerList;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.DatarouterNodes;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.timer.PhaseTimer;

public class HBaseCompactionJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(HBaseCompactionJob.class);

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

	private final LongAdder numTables = new LongAdder();
	private final LongAdder numRegions = new LongAdder();
	private final LongAdder numMissingLoad = new LongAdder();
	private final LongAdder numAlreadyLocal = new LongAdder();
	private final LongAdder numTriggered = new LongAdder();

	@Override
	public void run(TaskTracker tracker){
		for(ClientId clientId : compactionInfo.getManagedClientIds()){
			Admin admin = hBaseClientManager.getAdmin(clientId);
			DrServerList servers = new DrServerList(admin);
			List<String> tableNames = nodes.getTableNamesForClient(clientId.getName());
			for(String tableName : tableNames){
				if(tracker.heartbeat(numRegions.sum()).shouldStop()){
					return;
				}
				try{
					compactTable(tracker, admin, servers, clientId, tableName);
				}catch(Exception e){
					logger.warn("error compacting {}.{}", clientId, tableName, e);
				}
			}
		}
		logger.warn("numTables={}, numRegions={}, numMissingLoad={}, numAlreadyLocal={}, numTriggered={}", numTables,
				numRegions, numMissingLoad, numAlreadyLocal, numTriggered);
	}

	private void compactTable(
			TaskTracker tracker,
			Admin admin,
			DrServerList servers,
			ClientId clientId,
			String tableName){
		numTables.increment();
		PhysicalNode<?,?,?> physicalNodeForTable = nodes.getPhysicalNodeForClientAndTable(clientId.getName(),
				tableName);
		if(physicalNodeForTable == null){
			logger.error("physicalNode not found for table " + tableName);
			return;
		}
		DrRegionList regionList = drRegionListFactory.make(clientId, servers, tableName, physicalNodeForTable,
				balancerFactory.getBalancerForTable(clientId, tableName));
		LongAdder numRegionsTriggeredInTable = new LongAdder();
		for(DrRegionInfo<?> region : regionList.getRegions()){
			if(tracker.heartbeat(numRegions.sum()).shouldStop()){
				return;
			}
			numRegions.increment();
			if(region.getLoad() == null){
				numMissingLoad.increment();
				logger.warn("region.getLoad()==null on {} {}", region.getTableName(), region.getName());
				continue;
			}
			if(region.getLoad().getDataLocality() == 1F){
				numAlreadyLocal.increment();
				continue;
			}
			DrhCompactionScheduler<?> scheduler = new DrhCompactionScheduler<>(compactionInfo, region);
			if(scheduler.shouldCompact()){
				compactRegion(admin, region, numRegionsTriggeredInTable);
			}
		}
	}

	private void compactRegion(Admin admin, DrRegionInfo<?> region, LongAdder numRegionsTriggeredInTable){
		numTriggered.increment();
		numRegionsTriggeredInTable.increment();
		PhaseTimer timer = new PhaseTimer("compact " + numRegionsTriggeredInTable + " of " + region.getTableName());
		String encodedRegionNameString = region.getRegion().getEncodedName();
		try{
			admin.majorCompactRegion(region.getRegion().getRegionName());
			Object startKey = region.getStartKeyString();
			// trailing space because expected for formating in timer.toString
			String timerMessage = String.format("major_compact server=%s table=%s region=%s startKey=%s ",
					region.getServerName(), region.getTableName(), encodedRegionNameString, startKey);
			timer.add(timerMessage);
			logger.warn(timer.toString());
		}catch(Exception e){
			logger.warn("failed to compact region:{} because of:", encodedRegionNameString, e);
		}
	}

}
