package com.hotpads.datarouter.client.imp.memcached;

import com.hotpads.datarouter.client.ClientFactory;

public interface MemcachedClientFactory 
extends ClientFactory{

	
//	@Override
//	public HBaseClient createClient(DataRouter router, String name, Properties properties, Map<String,Object> params)
//			throws Exception;
	
	@Override
	public MemcachedClient getClient();
}
