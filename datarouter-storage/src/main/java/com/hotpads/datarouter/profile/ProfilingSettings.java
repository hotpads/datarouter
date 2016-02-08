package com.hotpads.datarouter.profile;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;

@Singleton
public class ProfilingSettings extends SettingNode{

	private final Setting<Boolean> saveCounts;


	@Inject
	public ProfilingSettings(SettingFinder finder){
		super(finder, "datarouter.profiling.", "datarouter.");

		saveCounts = registerBoolean("saveCounts", true);
	}


	public Setting<Boolean> getSaveCounts(){
		return saveCounts;
	}

}
