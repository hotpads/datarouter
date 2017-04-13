package com.hotpads.example.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;

@Singleton
public class ExampleDatarouterProperties extends DatarouterProperties{

	public static final String CONFIG_DIRECTORY = "/hotpads/config";
	public static final String SERVER_CONFIG_FILE_NAME = "server.properties";
	public static final String EXAMPLE_ROUTER_CONFIG_FILE_NAME = "datarouter-example.properties";

	@Inject
	public ExampleDatarouterProperties(ExampleConfigurer configurer){
		super(configurer, ExampleServerType.ALL, CONFIG_DIRECTORY, SERVER_CONFIG_FILE_NAME);
		assertConfigFileExists(EXAMPLE_ROUTER_CONFIG_FILE_NAME);
	}


	public String getReputationRouterConfigFileLocation(){
		return configDirectory + "/" + EXAMPLE_ROUTER_CONFIG_FILE_NAME;
	}

}
