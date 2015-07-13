package com.hotpads.datarouter.client.imp.jdbc;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.setting.DatarouterSettings;
import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.setting.cluster.SettingNode;
import com.hotpads.util.core.cache.Cached;

@Singleton
public class JdbcSettings extends SettingNode{
	
	private Cached<Integer> largeLookupAlertThreshold;

	@Inject
	public JdbcSettings(ClusterSettingFinder finder, DatarouterSettings datarouterSettings){
		super(finder, datarouterSettings.getName() + "jdbc.", datarouterSettings);
		registerSettings();
	}

	private void registerSettings(){
		largeLookupAlertThreshold = registerInteger("largeLookupAlertThreshold", 10000);
	}
	
	public Cached<Integer> getLargeLookupAlertThreshold(){
		return largeLookupAlertThreshold;
	}

}
