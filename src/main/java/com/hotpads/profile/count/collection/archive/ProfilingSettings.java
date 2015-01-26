package com.hotpads.profile.count.collection.archive;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.WebAppName;
import com.hotpads.setting.Setting;
import com.hotpads.setting.cluster.ClusterSettingFinder;
import com.hotpads.setting.cluster.SettingNode;

@Singleton
public class ProfilingSettings extends SettingNode{
	
	private Setting<Boolean> saveCounts;
	
	@Inject
	public ProfilingSettings(ClusterSettingFinder finder, WebAppName webAppName) {
		super(finder, webAppName + ".profiling.", webAppName + ".");
		registerSettings();
	}
	
	private void registerSettings(){
		saveCounts = registerBoolean("saveCounts", true);
	}
	
	/*******************leaf getters*******************/

	public Setting<Boolean> getSaveCounts() {
		return saveCounts;
	}
}
