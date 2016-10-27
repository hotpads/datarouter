package com.hotpads.datarouter.config;

import com.hotpads.datarouter.setting.ServerType;

public interface DatarouterProperties{
	String getServerPublicIp();
	String getServerName();
	String getServerTypeString();
	ServerType getServerType();
	String getAdministratorEmail();
	String getConfigPath();
}
