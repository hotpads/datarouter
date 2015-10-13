package com.hotpads.datarouter.client.imp.hbase.balancer.imp;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.hbase.ServerName;

import com.hotpads.datarouter.client.imp.hbase.balancer.BaseHBaseRegionBalancer;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class RoundRobinBalancer
extends BaseHBaseRegionBalancer{
	
	public RoundRobinBalancer(String tableName){
		super(tableName);
	}

	
	@Override
	public Map<DRHRegionInfo<?>,ServerName> call() {
		this.serverByRegion = new TreeMap<>();
		List<ServerName> serverNames = drhServerList.getServerNamesSorted();
		
		int regionIndex=0;
		for(DRHRegionInfo<?> drhRegionInfo : drhRegionList.getRegionsSorted()){
			int serverIndex = regionIndex % DrCollectionTool.size(serverNames);
			this.serverByRegion.put(drhRegionInfo, serverNames.get(serverIndex));
			++regionIndex;
		}
		
		return serverByRegion;
	}
	
}
