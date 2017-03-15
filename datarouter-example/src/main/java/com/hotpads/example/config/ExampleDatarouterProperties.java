package com.hotpads.example.config;

import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;

@Singleton
public class ExampleDatarouterProperties extends DatarouterProperties{

	private static final String CONFIG_PATH = "/hotpads/config/datarouter-example.properties";


	public ExampleDatarouterProperties(){
		super(ExampleServerType.ALL, CONFIG_PATH);
	}

}
