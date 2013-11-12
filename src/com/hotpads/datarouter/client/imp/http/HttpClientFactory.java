package com.hotpads.datarouter.client.imp.http;

import java.util.List;
import java.util.Properties;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.util.core.PropertiesTool;


public class HttpClientFactory implements ClientFactory{

	private DataRouterContext drContext;
	private String clientName;
	private List<String> configFilePaths;
	private List<Properties> multiProperties;
	private HttpClientOptions options;
	
	private boolean initialized = false;
	private volatile HttpClient client = null;//volatile for double checked locking
	

	
	
	public HttpClientFactory(DataRouterContext drContext, String clientName){
		this.drContext = drContext;
		this.clientName = clientName;
		this.configFilePaths = drContext.getConfigFilePaths();
		this.multiProperties = PropertiesTool.fromFiles(configFilePaths);
		this.options = new HttpClientOptions(multiProperties, clientName);
	}

	
	@Override
	public boolean isInitialized(){
		return initialized;
	}
	
	@Override
	public HttpClient getClient(){
		if(client!=null){ return client; }//make sure client is volatile
		synchronized(this){
			if(client!=null){ return client; }
			client = new HttpClient(clientName, options.getUrl());
		}
		return client;
	}
	
}
