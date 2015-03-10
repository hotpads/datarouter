package com.hotpads.setting;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.setting.cached.imp.BooleanCachedSetting;
import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.setting.cluster.SettingNode;

@Singleton
public class DatarouterSettings extends SettingNode {

	private DatarouterNotificationSettings notificationSettings;

	private BooleanCachedSetting loggingConfigUpdaterEnabled;
	private BooleanCachedSetting recordCallsites;

	@Inject
	public DatarouterSettings(ClusterSettingFinder finder, DatarouterNotificationSettings notificationSettings){
		super(finder, "datarouter.", "");
		this.notificationSettings = notificationSettings;
		children.put(notificationSettings.getName(), notificationSettings);
		registerSettings();
	}

	private void registerSettings(){
		this.loggingConfigUpdaterEnabled = registerBoolean("loggingConfigUpdaterEnabled", true);
		this.recordCallsites = registerBoolean("recordCallsites", false);
	}

	public DatarouterNotificationSettings getNotificationSettings() {
		return notificationSettings;
	}

	public BooleanCachedSetting getLoggingConfigUpdaterEnabled(){
		return loggingConfigUpdaterEnabled;
	}

	public BooleanCachedSetting getRecordCallsites(){
		return recordCallsites;
	}
}
