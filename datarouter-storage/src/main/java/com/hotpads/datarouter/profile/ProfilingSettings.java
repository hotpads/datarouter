package com.hotpads.datarouter.profile;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.setting.Setting;
import com.hotpads.setting.cluster.SettingFinder;
import com.hotpads.setting.cluster.SettingNode;

@Singleton
public class ProfilingSettings extends SettingNode{

	private Setting<Boolean> saveCounts;

	@Inject
	public ProfilingSettings(SettingFinder finder){
		super(finder, "datarouter.profiling.", "datarouter.");
		registerSettings();
	}

	private void registerSettings(){
		saveCounts = registerBoolean("saveCounts", true);
	}

	/*******************leaf getters*******************/

	public Setting<Boolean> getSaveCounts(){
		return saveCounts;
	}

}
