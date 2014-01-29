package com.hotpads.datarouter.client.imp.hbase.factory;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.type.HBaseClient;

public interface HBaseClientFactory 
extends ClientFactory{

	
//	@Override
//	public HBaseClient createClient(DataRouter router, String name, Properties properties, Map<String,Object> params)
//			throws Exception;
	
	@Override
	public HBaseClient getClient();
}
