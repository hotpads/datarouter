package com.hotpads.job.config.setting.job;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.job.setting.ClusterSettingFinder;
import com.hotpads.job.setting.Setting;
import com.hotpads.job.setting.SettingNode;
import com.hotpads.job.setting.cached.imp.BooleanCachedSetting;

@Singleton
public class ProfilingSettings extends SettingNode{
	
	protected Setting<Boolean> saveCounts = new BooleanCachedSetting(finder, getName()+"saveCounts", true);
	
	@Inject
	public ProfilingSettings(ClusterSettingFinder finder) {
		super(finder, "job.profiling.", "job.");
		this.finder = finder;
	}
	
	
	/*******************leaf getters*******************/

	public Setting<Boolean> getSaveCounts() {
		return saveCounts;
	}

}
