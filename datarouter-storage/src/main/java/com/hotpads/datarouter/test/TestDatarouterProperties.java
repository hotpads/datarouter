package com.hotpads.datarouter.test;

import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.setting.StandardServerType;

@Singleton
public class TestDatarouterProperties extends DatarouterProperties{

	public static final String CONFIG_DIRECTORY = "/hotpads/config";
	public static final String SERVER_CONFIG_FILE_NAME = "server.properties";
	public static final String TEST_ROUTER_CONFIG_FILE_NAME = "datarouter-test.properties";

	public TestDatarouterProperties(){
		super(StandardServerType.ALL, CONFIG_DIRECTORY, SERVER_CONFIG_FILE_NAME);
	}

	public String getTestRouterConfigFileLocation(){
		return configDirectory + "/" + TEST_ROUTER_CONFIG_FILE_NAME;
	}

}