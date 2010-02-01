package com.hotpads.datarouter.client;

import java.util.Map;
import java.util.Properties;

import com.hotpads.datarouter.DataRouterFactory;
import com.hotpads.datarouter.routing.DataRouter;

public interface ClientFactory {

	Client createClient(
			DataRouterFactory<? extends DataRouter> datapus, 
			String name, 
			Properties properties, 
			Map<String,Object> params) 
	throws Exception;
	
}
