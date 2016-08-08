package com.hotpads.example.config;

import javax.inject.Singleton;

import com.hotpads.datarouter.config.BaseDatarouterProperties;

@Singleton
public class ExampleDatarouterProperties extends BaseDatarouterProperties{

	private static final String CONFIG_PATH = "/hotpads/config/datarouter-example.properties";


	public ExampleDatarouterProperties(){
		super(CONFIG_PATH);
	}

}
