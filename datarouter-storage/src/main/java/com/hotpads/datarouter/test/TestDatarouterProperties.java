package com.hotpads.datarouter.test;

import javax.inject.Singleton;

import com.hotpads.datarouter.config.BaseDatarouterProperties;

@Singleton
public class TestDatarouterProperties extends BaseDatarouterProperties{

	private static final String CONFIG_PATH = "/hotpads/config/datarouter-test.properties";

	public TestDatarouterProperties(){
		super(CONFIG_PATH);
	}

	@Override
	public String getConfigPath(){
		return CONFIG_PATH;
	}

}