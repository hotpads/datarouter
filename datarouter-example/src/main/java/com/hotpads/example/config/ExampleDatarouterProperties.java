package com.hotpads.example.config;

import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;

@Singleton
public class ExampleDatarouterProperties extends DatarouterProperties{

	public ExampleDatarouterProperties(){
		super(ExampleServerType.ALL, "/hotpads/config", "datarouter-example.properties");
	}

}
