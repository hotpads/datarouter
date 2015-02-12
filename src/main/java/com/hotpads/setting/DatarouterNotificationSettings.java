package com.hotpads.setting;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.setting.cluster.SettingNode;

@Singleton
public class DatarouterNotificationSettings extends SettingNode{

	private Setting<String> apiEndPoint;
	private Setting<Boolean> forceHideStackTrace;
	private Setting<Boolean> ignoreSsl;

	@Inject
	public DatarouterNotificationSettings(ClusterSettingFinder finder){
		super(finder, "datarouter.notification.", "datarouter.");
		register();
	}

	private void register(){
		apiEndPoint = registerString("apiEndPoint", "https://localhost:8443/job/api/notification");
		forceHideStackTrace = registerBoolean("forceHideStackTrace", false);
		ignoreSsl = registerBoolean("ignoreSsl", false);
	}

	public Setting<String> getApiEndPoint(){
		return apiEndPoint;
	}

	public Setting<Boolean> getForceHideStackTrace(){
		return forceHideStackTrace;
	}

	public Setting<Boolean> getIgnoreSsl(){
		return ignoreSsl;
	}

}
