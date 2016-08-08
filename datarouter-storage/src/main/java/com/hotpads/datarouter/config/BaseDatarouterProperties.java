package com.hotpads.datarouter.config;

import java.util.Properties;

import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public abstract class BaseDatarouterProperties implements DatarouterProperties{

	private static final String SERVER_NAME = "server.name";
	private static final String ADMINISTRATOR_EMAIL = "administrator.email";

	private final Properties properties;

	public BaseDatarouterProperties(String path){
		properties = DrPropertiesTool.parse(path);
		if (DrStringTool.isNullOrEmptyOrWhitespace(getServerName())) {
			throw new RuntimeException("Expected " + path + " to contain property " + SERVER_NAME);
		}
		if (DrStringTool.isNullOrEmptyOrWhitespace(getAdministratorEmail())) {
			throw new RuntimeException("Expected " + path + " to contain property " + ADMINISTRATOR_EMAIL);
		}
	}

	@Override
	public String getServerName(){
		return properties.getProperty(SERVER_NAME);
	}

	@Override
	public String getAdministratorEmail(){
		return properties.getProperty(ADMINISTRATOR_EMAIL);
	}

}
