package com.hotpads.example.config;

import java.util.Objects;

import javax.inject.Singleton;

import com.hotpads.datarouter.config.BaseDatarouterProperties;
import com.hotpads.datarouter.setting.ServerType;
import com.hotpads.datarouter.setting.StandardServerType;

@Singleton
public class ExampleDatarouterProperties extends BaseDatarouterProperties{

	private static final String CONFIG_PATH = "/hotpads/config/datarouter-example.properties";


	public ExampleDatarouterProperties(){
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
