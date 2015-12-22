package com.hotpads.datarouter.batch.config;

import javax.inject.Inject;

import com.hotpads.setting.cluster.SettingFinder;
import com.hotpads.setting.cluster.SettingNode;
import com.hotpads.util.core.cache.Cached;

public class BatchSizeOptimizerSettings extends SettingNode{

	private Cached<Boolean> runBatchSizeOptimizingJob;
	private Cached<Boolean> runOpPerformanceRecordAggregationJob;

	@Inject
	public BatchSizeOptimizerSettings(SettingFinder finder){
		super(finder, "datarouter.batch.", "datarouter.");
		registerSettings();
	}

	private void registerSettings(){
		runBatchSizeOptimizingJob = registerBoolean("runBatchSizeOptimizingJob", false);
		runOpPerformanceRecordAggregationJob = registerBoolean("runOpPerformanceRecordAggregationJob", false);
	}

	public Cached<Boolean> getRunBatchSizeOptimizingJob(){
		return runBatchSizeOptimizingJob;
	}

	public Cached<Boolean> getRunOpPerformanceRecordAggregationJob(){
		return runOpPerformanceRecordAggregationJob;
	}

}
