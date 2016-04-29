package com.hotpads.datarouter.profile;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;

@Singleton
public class ProfilingSettings extends SettingNode{

	private final Setting<Boolean> saveCounts;
	private final Setting<Boolean> runMetricsAggregationJob;

	@Inject
	public ProfilingSettings(SettingFinder finder){
		super(finder, "datarouter.profiling.", "datarouter.");

		saveCounts = registerBoolean("saveCounts", true);
		runMetricsAggregationJob = registerBoolean("runMetricsAggregationJob", false);
	}

	public Setting<Boolean> getSaveCounts(){
		return saveCounts;
	}

	public Setting<Boolean> getRunMetricsAggregationJob(){
		return runMetricsAggregationJob;
	}

}
