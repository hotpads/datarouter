package com.hotpads.datarouter.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;

@Singleton
public class DatarouterNotificationSettings extends SettingNode{

	private Setting<String> apiEndPoint;
	private Setting<Boolean> forceHideStackTrace;
	private Setting<Boolean> ignoreSsl;

	@Inject
	public DatarouterNotificationSettings(SettingFinder finder){
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
