package com.hotpads.datarouter.test;

import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.setting.StandardServerType;

@Singleton
public class TestDatarouterProperties extends DatarouterProperties{

	private static final String SERVICE_NAME = "datarouter-test";
	public static final String CONFIG_DIRECTORY = "/hotpads/config";
	public static final String SERVER_CONFIG_FILE_NAME = "server.properties";
	public static final String DATAROUTER_TEST_FILE_NAME = "datarouter-test.properties";

	private final String datarouterTestFileLocation;


	public TestDatarouterProperties(){
		super(null, StandardServerType.ALL, SERVICE_NAME, CONFIG_DIRECTORY, SERVER_CONFIG_FILE_NAME);
		this.datarouterTestFileLocation = findConfigFile(DATAROUTER_TEST_FILE_NAME);
	}


	public String getDatarouterTestFileLocation(){
		return datarouterTestFileLocation;
	}

}