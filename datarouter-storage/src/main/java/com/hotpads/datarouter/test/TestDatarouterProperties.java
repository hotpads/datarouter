package com.hotpads.datarouter.test;

import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.setting.StandardServerType;

@Singleton
public class TestDatarouterProperties extends DatarouterProperties{

	private static final String CONFIG_PATH = "/hotpads/config/datarouter-test.properties";

	public TestDatarouterProperties(){
		super(StandardServerType.UNKNOWN, CONFIG_PATH);
	}

}