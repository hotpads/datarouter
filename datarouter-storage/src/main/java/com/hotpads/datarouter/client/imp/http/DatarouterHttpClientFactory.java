package com.hotpads.datarouter.client.imp.http;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.util.core.DrPropertiesTool;


public class DatarouterHttpClientFactory implements ClientFactory{

	private String clientName;
	private Set<String> configFilePaths;
	private List<Properties> multiProperties;
	private DatarouterHttpClientOptions options;
	private final ClientAvailabilitySettings clientAvailabilitySettings;

	public DatarouterHttpClientFactory(Datarouter datarouter, String clientName, ClientAvailabilitySettings
			clientAvailabilitySettings){
		this.clientName = clientName;
		this.clientAvailabilitySettings = clientAvailabilitySettings;
		this.configFilePaths = datarouter.getConfigFilePaths();
		this.multiProperties = DrPropertiesTool.fromFiles(configFilePaths);
		this.options = new DatarouterHttpClientOptions(multiProperties, clientName);
	}

	@Override
	public Client call(){
		return new DatarouterHttpClient(clientName, options.getUrl(), clientAvailabilitySettings);
	}

}
