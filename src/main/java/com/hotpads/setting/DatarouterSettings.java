package com.hotpads.setting;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.setting.cluster.SettingNode;

@Singleton
public class DatarouterSettings extends SettingNode {

	private NotificationSettings notificationSettings;

	@Inject
	public DatarouterSettings(ClusterSettingFinder finder, NotificationSettings notificationSettings) {
		super(finder, "datarouter.", "");
		this.notificationSettings = notificationSettings;
		children.put(notificationSettings.getName(), notificationSettings);
	}

	public NotificationSettings getNotificationSettings() {
		return notificationSettings;
	}

}
