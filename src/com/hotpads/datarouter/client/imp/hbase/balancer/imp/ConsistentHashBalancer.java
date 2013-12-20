package com.hotpads.datarouter.client.imp.hbase.balancer.imp;

import java.util.SortedMap;

import org.apache.hadoop.hbase.ServerName;

import com.hotpads.datarouter.client.imp.hbase.balancer.BalanceLeveler;
import com.hotpads.datarouter.client.imp.hbase.balancer.BalancerStrategy;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerList;
import com.hotpads.util.core.HashMethods;
import com.hotpads.util.core.MapTool;

public class ConsistentHashBalancer
implements BalancerStrategy{
	
	public static final Integer BUCKETS_PER_NODE = 1000;
	
	protected SortedMap<DRHRegionInfo<?>,ServerName> serverByRegion;

	
	/******************* constructor ***************************/
	
	public ConsistentHashBalancer(){//no-arg for reflection
		this.serverByRegion = MapTool.createTreeMap();
	}
	
	
	public ConsistentHashBalancer initMappings(DRHServerList servers, DRHRegionList regions){
		//set up the ring of servers
		SortedMap<Long,ServerName> consistentHashRing = buildServerHashRing(servers, BUCKETS_PER_NODE);
		
		//calculate each region's position in the ring and store it
		for(DRHRegionInfo<?> drhRegionInfo : regions.getRegions()){
			byte[] consistentHashInput = drhRegionInfo.getRegion().getEncodedNameAsBytes();
			ServerName serverName = calcServerNameForItem(consistentHashRing, consistentHashInput);
			serverByRegion.put(drhRegionInfo, serverName);//now region->server mapping is known
		}
		
		//level out any imbalances from the hashing
		BalanceLeveler<DRHRegionInfo<?>,ServerName> leveler = new BalanceLeveler<DRHRegionInfo<?>,ServerName>(
				serverByRegion);
		serverByRegion = leveler.getBalancedDestinationByItem();
		
		return this;
	}
	
	
	@Override
	public ServerName getServerName(DRHRegionInfo<?> drhRegionInfo) {
		return serverByRegion.get(drhRegionInfo);
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
