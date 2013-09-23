package com.hotpads.job.config.instance;


import javax.inject.Singleton;

@Singleton
public class InstanceSettings {
	
	
	public InstanceSettings(){
		//TODO read from /hotpads/config/hotpads.properties
	}
	
	public ServerType getServerType(){
		return ServerType.UNKNOWN;
	}
	
	public String getInstance() {
		return "instance";
	}
	
	public String getApplication() {
		return "application";
	}
}
