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
package io.datarouter.client.hbase.balancer.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.hbase.ServerName;

import io.datarouter.client.hbase.balancer.BaseHBaseRegionBalancer;
import io.datarouter.client.hbase.balancer.HBaseBalanceLeveler;
import io.datarouter.client.hbase.cluster.DrRegionInfo;
import io.datarouter.util.Require;

/*
 * assign each partition in full to a server, and hard-level the number of regions
 */
public class EntityPartitionBalancer
extends BaseHBaseRegionBalancer{

	private Map<Integer,List<DrRegionInfo<?>>> regionsByPartition;

	public EntityPartitionBalancer(String tableName){
		super(tableName);
	}

	@Override
	public SortedMap<DrRegionInfo<?>,ServerName> call(){
		initRegionByPartitionMap();

		//set up the ring of servers
		SortedMap<Long,ServerName> consistentHashRing = ConsistentHashBalancer.buildServerHashRing(drhServerList,
				ConsistentHashBalancer.BUCKETS_PER_NODE);

		//calculate each partition's position in the ring and store it
		SortedMap<Integer,ServerName> serverByPartition = new TreeMap<>();
		for(Integer partition : regionsByPartition.keySet()){
			byte[] consistentHashInput = entityPartitioner.getPrefix(partition);
			ServerName serverName = ConsistentHashBalancer.calcServerNameForItem(consistentHashRing,
					consistentHashInput);
			serverByPartition.put(partition, serverName);//now region->server mapping is known
		}

		//level out any imbalances from the hashing
		HBaseBalanceLeveler<Integer> leveler = new HBaseBalanceLeveler<>(drhServerList.getServerNames(),
				serverByPartition, tableName);
		serverByPartition = leveler.getBalancedDestinationByItem();

		//map individual regions to servers based on their prefix
		for(Entry<Integer,ServerName> entry : serverByPartition.entrySet()){
			List<DrRegionInfo<?>> regionsInPartition = regionsByPartition.get(entry.getKey());
			for(DrRegionInfo<?> region : regionsInPartition){
				serverByRegion.put(region, entry.getValue());
			}
		}
		assertRegionCountsConsistent();
		return serverByRegion;
	}

	private void initRegionByPartitionMap(){
		regionsByPartition = new TreeMap<>();
		for(Integer partition : entityPartitioner.getAllPartitions()){
			regionsByPartition.put(partition, new ArrayList<>());
		}
		for(DrRegionInfo<?> drhRegionInfo : drhRegionList.getRegions()){
			Integer partition = drhRegionInfo.getPartition();
			if(partition == null){
				partition = 0;
			}
			Require.isTrue(regionsByPartition.containsKey(partition), "partition " + partition + " not found");
			regionsByPartition.get(partition).add(drhRegionInfo);
		}
	}

}
