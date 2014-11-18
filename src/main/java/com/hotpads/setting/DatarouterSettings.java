package com.hotpads.setting;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.setting.cached.imp.BooleanCachedSetting;
import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.setting.cluster.SettingNode;

@Singleton
public class DatarouterSettings extends SettingNode {

	private DatarouterNotificationSettings notificationSettings;
	private DatarouterSalesforceSettings salesforceSettings;
	
	private BooleanCachedSetting loggingConfigUpdaterEnabled;
	private BooleanCachedSetting recordCallsites;

	@Inject
	public DatarouterSettings(ClusterSettingFinder finder, DatarouterNotificationSettings notificationSettings,
			DatarouterSalesforceSettings salesforceSettings){
		super(finder, "datarouter.", "");
		this.notificationSettings = notificationSettings;
		this.salesforceSettings = salesforceSettings;
		children.put(notificationSettings.getName(), notificationSettings);
		children.put(salesforceSettings.getName(), salesforceSettings);
		registerSettings();
	}

	private void registerSettings(){
		this.loggingConfigUpdaterEnabled = registerBoolean(getName() + "loggingConfigUpdaterEnabled", true);
		this.recordCallsites = registerBoolean(getName() + "recordCallsites", false);
	}

	public DatarouterNotificationSettings getNotificationSettings() {
		return notificationSettings;
	}

	public DatarouterSalesforceSettings getSalesforceSettings(){
		return salesforceSettings;
	}

	public BooleanCachedSetting getLoggingConfigUpdaterEnabled(){
		return loggingConfigUpdaterEnabled;
	}
	
	public BooleanCachedSetting getRecordCallsites(){
		return recordCallsites;
	}
}
