package com.hotpads.datarouter.client.imp.hbase.balancer;

public interface HBaseBalancerFactory{

	BaseHBaseRegionBalancer getBalancerForTable(String tableName);
	
}
