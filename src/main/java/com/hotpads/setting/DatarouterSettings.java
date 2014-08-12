package com.hotpads.setting;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.setting.cluster.SettingNode;

@Singleton
public class DatarouterSettings extends SettingNode {

	private DatarouterNotificationSettings notificationSettings;
	private DatarouterSalesforceSettings salesforceSettings;

	@Inject
	public DatarouterSettings(ClusterSettingFinder finder, DatarouterNotificationSettings notificationSettings,
			DatarouterSalesforceSettings salesforceSettings){
		super(finder, "datarouter.", "");
		this.notificationSettings = notificationSettings;
		this.salesforceSettings = salesforceSettings;
		children.put(notificationSettings.getName(), notificationSettings);
		children.put(salesforceSettings.getName(), salesforceSettings);
	}

	public DatarouterNotificationSettings getNotificationSettings() {
		return notificationSettings;
	}

	public DatarouterSalesforceSettings getSalesforceSettings(){
		return salesforceSettings;
	}

}
