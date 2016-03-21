package com.hotpads.datarouter.batch.config;

import javax.inject.Inject;

import com.hotpads.datarouter.setting.SettingFinder;
import com.hotpads.datarouter.setting.SettingNode;
import com.hotpads.util.core.cache.Cached;

public class BatchSizeOptimizerSettings extends SettingNode{

	private final Cached<Boolean> runBatchSizeOptimizingJob;
	private final Cached<Boolean> runOpPerformanceRecordAggregationJob;
	private final Cached<Boolean> tableRowCountJob;


	@Inject
	public BatchSizeOptimizerSettings(SettingFinder finder){
		super(finder, "datarouter.batch.", "datarouter.");

		runBatchSizeOptimizingJob = registerBoolean("runBatchSizeOptimizingJob", false);
		runOpPerformanceRecordAggregationJob = registerBoolean("runOpPerformanceRecordAggregationJob", false);
		tableRowCountJob = registerBoolean("tableRowCount", false);
	}


	public Cached<Boolean> getRunBatchSizeOptimizingJob(){
		return runBatchSizeOptimizingJob;
	}

	public Cached<Boolean> getRunOpPerformanceRecordAggregationJob(){
		return runOpPerformanceRecordAggregationJob;
	}


	public Cached<Boolean> getTableRowCount(){
		return tableRowCountJob;
	}


}
