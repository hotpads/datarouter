package com.hotpads.datarouter.client.imp.hbase.balancer.imp;

import java.util.SortedMap;

import org.apache.hadoop.hbase.ServerName;

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
	
	public ConsistentHashBalancer(){//public no-arg for reflection
	}
	
	
	public ConsistentHashBalancer initMappings(DRHServerList servers, DRHRegionList regions){
		
		//set up the ring of servers
		SortedMap<Long,ServerName> consistentHashRing = MapTool.createTreeMap();
		for(DRHServerInfo server : servers.getServers()){
			for(int i = 0; i < BUCKETS_PER_NODE; ++i){
				long bucketPosition = HashMethods.longMD5DJBHash(
						server.getServerName().getHostAndPort()+i);
				consistentHashRing.put(bucketPosition, server.getServerName());
			}
		}

		
		//calculate each region's position in the ring and store it
		this.serverByRegion = MapTool.createTreeMap();
		for(DRHRegionInfo<?> drhRegionInfo : regions.getRegions()){
			byte[] consistentHashInput = drhRegionInfo.getRegion().getEncodedNameAsBytes();
			long hash = HashMethods.longMD5DJBHash(consistentHashInput);
			if(consistentHashRing.isEmpty()){ continue; }
			if(!consistentHashRing.containsKey(hash)){
				SortedMap<Long,ServerName> tail = consistentHashRing.tailMap(hash);
				hash = tail.isEmpty() ? consistentHashRing.firstKey() : tail.firstKey();
			}
			ServerName serverName = consistentHashRing.get(hash);
			this.serverByRegion.put(drhRegionInfo, serverName);//now region->server mapping is known
		}
		
		return this;
	}
	
	
	@Override
	public ServerName getServerName(DRHRegionInfo<?> drhRegionInfo) {
		return serverByRegion.get(drhRegionInfo);
	}
	
}
