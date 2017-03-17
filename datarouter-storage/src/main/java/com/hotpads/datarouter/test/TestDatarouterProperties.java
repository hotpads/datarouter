package com.hotpads.datarouter.test;

import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.setting.StandardServerType;

@Singleton
public class TestDatarouterProperties extends DatarouterProperties{

	public TestDatarouterProperties(){
		super(StandardServerType.ALL, "/hotpads/config", "datarouter-test.properties");
	}

}