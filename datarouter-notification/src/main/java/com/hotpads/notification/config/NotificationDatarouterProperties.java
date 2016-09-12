package com.hotpads.notification.config;

import javax.inject.Singleton;

import com.hotpads.datarouter.config.BaseDatarouterProperties;

@Singleton
public class NotificationDatarouterProperties extends BaseDatarouterProperties{

	private static final String CONFIG_PATH = "/hotpads/config/datarouter-example.properties";


	public NotificationDatarouterProperties(){
		super(CONFIG_PATH);
	}

	@Override
	public String getConfigPath(){
		return CONFIG_PATH;
	}

}
