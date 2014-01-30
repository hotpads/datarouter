package com.hotpads.datarouter.client.imp.hbase.balancer.imp;

import java.util.Map;
import java.util.SortedMap;

import org.apache.hadoop.hbase.ServerName;

import com.hotpads.datarouter.client.imp.hbase.balancer.BalanceLeveler;
import com.hotpads.datarouter.client.imp.hbase.balancer.BaseHBaseRegionBalancer;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerList;
import com.hotpads.util.core.HashMethods;
import com.hotpads.util.core.MapTool;

public class ConsistentHashBalancer
extends BaseHBaseRegionBalancer{
	
	public static final Integer BUCKETS_PER_NODE = 1000;

	
	@Override
	public Map<DRHRegionInfo<?>,ServerName> call(){
		//set up the ring of servers
		SortedMap<Long,ServerName> consistentHashRing = buildServerHashRing(drhServerList, BUCKETS_PER_NODE);
		
		//calculate each region's position in the ring and store it
		for(DRHRegionInfo<?> drhRegionInfo : drhRegionList.getRegions()){
			byte[] consistentHashInput = drhRegionInfo.getRegion().getEncodedNameAsBytes();
			ServerName serverName = calcServerNameForItem(consistentHashRing, consistentHashInput);
			serverByRegion.put(drhRegionInfo, serverName);//now region->server mapping is known
		}
//		logger.warn(getServerByRegionStringForDebug());
		assertRegionCountsConsistent();
		
		//level out any imbalances from the hashing
		BalanceLeveler<DRHRegionInfo<?>,ServerName> leveler = new BalanceLeveler<DRHRegionInfo<?>,ServerName>(
				drhServerList.getServerNames(), serverByRegion);
		serverByRegion = leveler.getBalancedDestinationByItem();

//		logger.warn(getServerByRegionStringForDebug());
		assertRegionCountsConsistent();
		return serverByRegion;
	}
	
	public static SortedMap<Long,ServerName> buildServerHashRing(DRHServerList servers, int numBucketsPerNode){
		SortedMap<Long,ServerName> consistentHashRing = MapTool.createTreeMap();
		for(DRHServerInfo server : servers.getServers()){
			for(int i = 0; i < numBucketsPerNode; ++i){
				long bucketPosition = HashMethods.longMD5DJBHash(server.getServerName().getHostAndPort() + i);
				consistentHashRing.put(bucketPosition, server.getServerName());
			}
		}
		return consistentHashRing;
	}
	
	public static ServerName calcServerNameForItem(SortedMap<Long,ServerName> consistentHashRing, 
			byte[] consistentHashInput){
		long hash = HashMethods.longMD5DJBHash(consistentHashInput);
		if(!consistentHashRing.containsKey(hash)){
			SortedMap<Long,ServerName> tail = consistentHashRing.tailMap(hash);
			hash = tail.isEmpty() ? consistentHashRing.firstKey() : tail.firstKey();
		}
		ServerName serverName = consistentHashRing.get(hash);
		return serverName;
	}
	
}
