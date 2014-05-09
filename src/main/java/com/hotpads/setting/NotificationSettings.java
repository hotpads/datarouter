package com.hotpads.setting;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.hotpads.setting.cached.imp.BooleanCachedSetting;
import com.hotpads.setting.cached.imp.StringCachedSetting;
import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.setting.cluster.SettingNode;

@Singleton
public class NotificationSettings extends SettingNode {

	private Setting<String> apiEndPoint;
	private Setting<Boolean> forceHideStackTrace;
	private Setting<Boolean> exceptionHandling;

	@Inject
	public NotificationSettings(ClusterSettingFinder finder) {
		super(finder, "datarouter.notification.", "datarouter.");
		register();
	}

	private void register() {
		apiEndPoint = register(new StringCachedSetting(finder, "apiEndPoint", "https://localhost:8443/job/api/notification"));
		forceHideStackTrace = register(new BooleanCachedSetting(finder, "forceHideStackTrace", false));
		exceptionHandling = register(new BooleanCachedSetting(finder, "exceptionHandling", false));
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
}
