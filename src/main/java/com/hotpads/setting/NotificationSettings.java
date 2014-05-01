package com.hotpads.setting;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.hotpads.setting.cached.imp.StringCachedSetting;
import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.setting.cluster.SettingNode;

@Singleton
public class NotificationSettings extends SettingNode {

	private Setting<String> apiEndPoint;
	
	@Inject
	public NotificationSettings(ClusterSettingFinder finder) {
		super(finder, "datarouter.notification.", "datarouter.");
		regidter();
	}

	private void regidter() {
		apiEndPoint = register(new StringCachedSetting(finder, "apiEndPoint", "https://localhost:8443/job/api/notification"));
	}

	public Setting<String> getEndPoint() {
		return apiEndPoint;
	}
}
