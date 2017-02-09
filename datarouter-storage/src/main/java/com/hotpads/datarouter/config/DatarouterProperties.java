package com.hotpads.datarouter.config;

import java.util.Properties;

import javax.inject.Inject;

import com.hotpads.datarouter.setting.ServerType;
import com.hotpads.datarouter.util.core.DrPropertiesTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public abstract class DatarouterProperties{
	private static final String SERVER_PUBLIC_IP = "server.publicIp";
	private static final String SERVER_PRIVATE_IP = "server.privateIp";
	private static final String SERVER_NAME = "server.name";
	private static final String SERVER_TYPE = "server.type";
	private static final String ADMINISTRATOR_EMAIL = "administrator.email";

	private final String path;
	private final Properties properties;

	@Inject
	private ServerType serverType;

	public DatarouterProperties(String path){
		this.path = path;
		this.properties = DrPropertiesTool.parse(path);
		validatePropertyValue(SERVER_PUBLIC_IP, getServerPublicIp());
		validatePropertyValue(SERVER_PRIVATE_IP, getServerPrivateIp());
		validatePropertyValue(SERVER_NAME, getServerName());
		validatePropertyValue(SERVER_TYPE, getServerTypeString());
		validatePropertyValue(ADMINISTRATOR_EMAIL, getAdministratorEmail());
	}

	private void validatePropertyValue(String name, String value){
		if(DrStringTool.isNullOrEmptyOrWhitespace(value)){
			throw new RuntimeException("Expected " + path + " to contain property " + name);
		}
	}

	public String getServerPublicIp(){
		return properties.getProperty(SERVER_PUBLIC_IP);
	}

	public String getServerPrivateIp(){
		return properties.getProperty(SERVER_PRIVATE_IP);
	}

	public String getServerName(){
		return properties.getProperty(SERVER_NAME);
	}

	public String getServerTypeString(){
		return properties.getProperty(SERVER_TYPE);
	}

	public ServerType getServerType(){
		return serverType.fromPersistentString(getServerTypeString());
	}

	public String getAdministratorEmail(){
		return properties.getProperty(ADMINISTRATOR_EMAIL);
	}

	public String getConfigPath(){
		return path;
	}

}
