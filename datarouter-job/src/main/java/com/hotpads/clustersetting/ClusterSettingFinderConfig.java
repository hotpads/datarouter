package com.hotpads.clustersetting;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.app.WebAppName;
import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.datarouter.setting.ServerType;

@Singleton
public class ClusterSettingFinderConfig{

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private WebAppName webAppName;

	public ServerType getServerType(){
		return datarouterProperties.getServerType();
	}

	public String getServerName(){
		return datarouterProperties.getServerName();
	}

	public String getApplication(){
		return webAppName.getName();
	}

}
