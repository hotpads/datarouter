package com.hotpads.datarouter.profile;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;

@Singleton
public class ProfilingSettings extends SettingNode{

	private final Setting<Boolean> saveCounts;
	private final Setting<Boolean> bufferCountsInSqs;//currently need to restart webapp after changing
	private final Setting<Boolean> drainSqsCounts;
	private final Setting<Boolean> runMetricsAggregationJob;
	private final Setting<Boolean> runServerMonitoringJob;
	public final Setting<Boolean> runLatencyMonitoringJob;
	public final Setting<Boolean> saveExecutorsMetrics;
	public final Setting<Boolean> saveHttpClientsMetrics;

	@Inject
	public ProfilingSettings(SettingFinder finder){
		super(finder, "datarouter.profiling.", "datarouter.");

		saveCounts = registerBoolean("saveCounts", true);
		bufferCountsInSqs = registerBoolean("bufferCountsInSqs", false);
		drainSqsCounts = registerBoolean("drainSqsCounts", false);
		runMetricsAggregationJob = registerBoolean("runMetricsAggregationJob", false);
		runServerMonitoringJob = registerBoolean("runServerMonitoringJob", true);
		runLatencyMonitoringJob = registerBoolean("runLatencyMonitoringJob", false);
		saveExecutorsMetrics = registerBoolean("saveExecutorsMetrics", false);
		saveHttpClientsMetrics = registerBoolean("saveHttpClientsMetrics", false);
	}

	public Setting<Boolean> getSaveCounts(){
		return saveCounts;
	}

	public Setting<Boolean> getBufferCountsInSqs(){
		return bufferCountsInSqs;
	}

	public Setting<Boolean> getDrainSqsCounts(){
		return drainSqsCounts;
	}

	public Setting<Boolean> getRunMetricsAggregationJob(){
		return runMetricsAggregationJob;
	}

	public Setting<Boolean> getRunServerMonitoringJob(){
		return runServerMonitoringJob;
	}

}
