package com.hotpads.datarouter.client;

import java.util.Map;
import java.util.Properties;

import com.hotpads.datarouter.routing.DataRouter;

public interface ClientFactory {

	Client createClient(
			DataRouter router, 
			String name, 
			Properties properties, 
			Map<String,Object> params) 
	throws Exception;
	
}
