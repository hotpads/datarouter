package com.hotpads.setting;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.setting.cluster.SettingNode;

@Singleton
public class DatarouterSettings extends SettingNode {

	private DatarouterNotificationSettings notificationSettings;

	@Inject
	public DatarouterSettings(ClusterSettingFinder finder, DatarouterNotificationSettings notificationSettings) {
		super(finder, "datarouter.", "");
		this.notificationSettings = notificationSettings;
		children.put(notificationSettings.getName(), notificationSettings);
	}

	public DatarouterNotificationSettings getNotificationSettings() {
		return notificationSettings;
	}

}
