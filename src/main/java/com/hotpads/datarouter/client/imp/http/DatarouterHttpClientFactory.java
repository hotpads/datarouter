package com.hotpads.datarouter.client.imp.http;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.util.core.PropertiesTool;


public class DatarouterHttpClientFactory implements ClientFactory{

	private String clientName;
	private Set<String> configFilePaths;
	private List<Properties> multiProperties;
	private DatarouterHttpClientOptions options;
	
	
	
	public DatarouterHttpClientFactory(DatarouterContext drContext, String clientName){
		this.clientName = clientName;
		this.configFilePaths = drContext.getConfigFilePaths();
		this.multiProperties = PropertiesTool.fromFiles(configFilePaths);
		this.options = new DatarouterHttpClientOptions(multiProperties, clientName);
	}

	
	@Override
	public Client call(){
		return new DatarouterHttpClient(clientName, options.getUrl());
	}
	
	
}
