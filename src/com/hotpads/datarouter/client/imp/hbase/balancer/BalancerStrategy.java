package com.hotpads.datarouter.client.imp.hbase.balancer;

import org.apache.hadoop.hbase.ServerName;

import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionInfo;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHRegionList;
import com.hotpads.datarouter.client.imp.hbase.cluster.DRHServerList;

public interface BalancerStrategy {

	BalancerStrategy initMappings(DRHServerList servers, DRHRegionList regions);
	ServerName getServerName(DRHRegionInfo<?> region);
	
}
