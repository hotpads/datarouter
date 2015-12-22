package com.hotpads.setting;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.setting.cluster.SettingFinder;
import com.hotpads.setting.cluster.SettingNode;
import com.hotpads.util.http.security.UrlScheme;

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
		apiEndPoint = registerString("apiEndPoint", UrlScheme.LOCAL_DEV_SERVER_HTTPS_URL + "/job/api/notification");
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
