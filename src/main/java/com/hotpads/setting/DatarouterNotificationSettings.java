package com.hotpads.setting;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.hotpads.setting.cached.imp.BooleanCachedSetting;
import com.hotpads.setting.cached.imp.StringCachedSetting;
import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.setting.cluster.SettingNode;

@Singleton
public class DatarouterNotificationSettings extends SettingNode {

	private Setting<String> apiEndPoint;
	private Setting<Boolean> forceHideStackTrace;
	private Setting<Boolean> exceptionHandling;
	private Setting<Boolean> ignoreSsl;

	@Inject
	public DatarouterNotificationSettings(ClusterSettingFinder finder) {
		super(finder, "datarouter.notification.", "datarouter.");
		register();
	}

	private void register() {
		apiEndPoint = register(new StringCachedSetting(finder, getName() + "apiEndPoint", "https://localhost:8443/job/api/notification"));
		forceHideStackTrace = register(new BooleanCachedSetting(finder, getName() + "forceHideStackTrace", false));
		exceptionHandling = register(new BooleanCachedSetting(finder, getName() + "exceptionHandling", false));
		ignoreSsl = register(new BooleanCachedSetting(finder, getName() + "ignoreSsl", false));
	}

	public Setting<String> getApiEndPoint() {
		return apiEndPoint;
	}

	public Setting<Boolean> getForceHideStackTrace() {
		return forceHideStackTrace;
	}

	public Setting<Boolean> getExceptionHandling() {
		return exceptionHandling;
	}

	public Setting<Boolean> getIgnoreSsl() {
		return ignoreSsl;
	}
}
