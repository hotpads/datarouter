package com.hotpads.datarouter.client.imp.http;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.util.core.PropertiesTool;


public class DataRouterHttpClientFactory implements ClientFactory{

	private DataRouterContext drContext;
	private String clientName;
	private Set<String> configFilePaths;
	private List<Properties> multiProperties;
	private DataRouterHttpClientOptions options;
	
	private boolean initialized = false;
	private volatile DataRouterHttpClient client = null;//volatile for double checked locking
	

	
	
	public DataRouterHttpClientFactory(DataRouterContext drContext, String clientName){
		this.drContext = drContext;
		this.clientName = clientName;
		this.configFilePaths = drContext.getConfigFilePaths();
		this.multiProperties = PropertiesTool.fromFiles(configFilePaths);
		this.options = new DataRouterHttpClientOptions(multiProperties, clientName);
	}

	
	@Override
	public boolean isInitialized(){
		return initialized;
	}
	
	@Override
	public DataRouterHttpClient getClient(){
		if(client!=null){ return client; }//make sure client is volatile
		synchronized(this){
			if(client!=null){ return client; }
			client = new DataRouterHttpClient(clientName, options.getUrl());
		}
		return client;
	}
	
}
