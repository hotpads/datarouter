package com.hotpads.datarouter.client.imp.hbase.balancer.imp;

import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.ServerName;

import com.hotpads.datarouter.client.imp.hbase.balancer.BaseHBaseRegionBalancer;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;
import com.hotpads.datarouter.util.core.CollectionTool;
import com.hotpads.datarouter.util.core.MapTool;

public class RoundRobinBalancer
extends BaseHBaseRegionBalancer{
	
	@Override
	public Map<DRHRegionInfo<?>,ServerName> call() {
		this.serverByRegion = MapTool.createTreeMap();
		List<ServerName> serverNames = drhServerList.getServerNamesSorted();
		
		int regionIndex=0;
		for(DRHRegionInfo<?> drhRegionInfo : drhRegionList.getRegionsSorted()){
			int serverIndex = regionIndex % CollectionTool.size(serverNames);
			this.serverByRegion.put(drhRegionInfo, serverNames.get(serverIndex));
			++regionIndex;
		}
		
		return serverByRegion;
	}
	
}
