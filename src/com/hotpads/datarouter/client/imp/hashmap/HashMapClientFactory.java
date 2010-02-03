package com.hotpads.datarouter.client.imp.hashmap;

import java.util.Map;
import java.util.Properties;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.routing.DataRouter;

public class HashMapClientFactory implements ClientFactory{

	@Override
	public HashMapClient createClient(
			DataRouter router, 
			String name, 
			Properties properties, 
			Map<String,Object> params){
		
		HashMapClient client = new HashMapClient();
		client.name = name;
		return client;

	}
	
}
