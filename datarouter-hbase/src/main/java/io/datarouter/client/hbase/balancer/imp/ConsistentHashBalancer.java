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

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.hbase.ServerName;

import io.datarouter.client.hbase.balancer.BaseHBaseRegionBalancer;
import io.datarouter.client.hbase.balancer.HBaseBalanceLeveler;
import io.datarouter.client.hbase.cluster.DrRegionInfo;
import io.datarouter.client.hbase.cluster.DrServerInfo;
import io.datarouter.client.hbase.cluster.DrServerList;
import io.datarouter.util.HashMethods;

public class ConsistentHashBalancer
extends BaseHBaseRegionBalancer{

	public static final Integer BUCKETS_PER_NODE = 1000;

	public ConsistentHashBalancer(String tableName){
		super(tableName);
	}

	@Override
	public Map<DrRegionInfo<?>,ServerName> call(){
		//set up the ring of servers
		SortedMap<Long,ServerName> consistentHashRing = buildServerHashRing(drhServerList, BUCKETS_PER_NODE);

		//calculate each region's position in the ring and store it
		for(DrRegionInfo<?> drhRegionInfo : drhRegionList.getRegions()){
			byte[] consistentHashInput = drhRegionInfo.getRegion().getEncodedNameAsBytes();
			ServerName serverName = calcServerNameForItem(consistentHashRing, consistentHashInput);
			serverByRegion.put(drhRegionInfo, serverName);//now region->server mapping is known
		}
		assertRegionCountsConsistent();

		//level out any imbalances from the hashing
		HBaseBalanceLeveler<DrRegionInfo<?>> leveler = new HBaseBalanceLeveler<>(drhServerList.getServerNames(),
				serverByRegion, tableName);
		serverByRegion = leveler.getBalancedDestinationByItem();

		assertRegionCountsConsistent();
		return serverByRegion;
	}

	public static SortedMap<Long,ServerName> buildServerHashRing(DrServerList servers, int numBucketsPerNode){
		SortedMap<Long,ServerName> consistentHashRing = new TreeMap<>();
		for(DrServerInfo server : servers.getServers()){
			for(int i = 0; i < numBucketsPerNode; ++i){
				long bucketPosition = HashMethods.longMd5DjbHash(server.getServerName().getHostAndPort() + i);
				consistentHashRing.put(bucketPosition, server.getServerName());
			}
		}
		return consistentHashRing;
	}

	public static ServerName calcServerNameForItem(
			SortedMap<Long,ServerName> consistentHashRing,
			byte[] consistentHashInput){
		long hash = HashMethods.longMd5DjbHash(consistentHashInput);
		if(!consistentHashRing.containsKey(hash)){
			SortedMap<Long,ServerName> tail = consistentHashRing.tailMap(hash);
			hash = tail.isEmpty() ? consistentHashRing.firstKey() : tail.firstKey();
		}
		ServerName serverName = consistentHashRing.get(hash);
		return serverName;
	}

}
