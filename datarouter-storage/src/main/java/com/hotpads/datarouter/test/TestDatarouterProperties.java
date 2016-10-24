package com.hotpads.datarouter.test;

import java.util.Objects;

import javax.inject.Singleton;

import com.hotpads.datarouter.config.BaseDatarouterProperties;
import com.hotpads.datarouter.setting.ServerType;
import com.hotpads.datarouter.setting.StandardServerType;

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

	@Override
	public ServerType getServerType(){
		return StandardServerType.fromPersistentStringStatic(Objects.requireNonNull(getServerTypeString()));
	}

}