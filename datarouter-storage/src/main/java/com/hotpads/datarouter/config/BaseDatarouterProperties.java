package com.hotpads.datarouter.config;

import java.util.Properties;

import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public abstract class BaseDatarouterProperties implements DatarouterProperties{
	private static final String SERVER_PUBLIC_IP = "server.publicIp";
	private static final String SERVER_NAME = "server.name";
	private static final String SERVER_TYPE = "server.type";
	private static final String ADMINISTRATOR_EMAIL = "administrator.email";

	private final String path;
	private final Properties properties;

	public BaseDatarouterProperties(String path){
		this.path = path;
		this.properties = DrPropertiesTool.parse(path);
		validatePropertyValue(SERVER_PUBLIC_IP, getServerPublicIp());
		validatePropertyValue(SERVER_NAME, getServerName());
		validatePropertyValue(SERVER_TYPE, getServerType());
		validatePropertyValue(ADMINISTRATOR_EMAIL, getAdministratorEmail());
	}

	private void validatePropertyValue(String name, String value){
		if(DrStringTool.isNullOrEmptyOrWhitespace(value)){
			throw new RuntimeException("Expected " + path + " to contain property " + name);
		}
	}

	@Override
	public String getServerPublicIp(){
		return properties.getProperty(SERVER_PUBLIC_IP);
	}

	@Override
	public String getServerName(){
		return properties.getProperty(SERVER_NAME);
	}

	@Override
	public String getServerType(){
		return properties.getProperty(SERVER_TYPE);
	}

	@Override
	public String getAdministratorEmail(){
		return properties.getProperty(ADMINISTRATOR_EMAIL);
	}

}
