package com.hotpads.job.joblet;

import com.hotpads.datarouter.profile.counter.Counters;

public class JobletCounters {
	public static final String
		PREFIX_QUEUE_LENGTH = "Joblet queue length",
		PREFIX_ITEMS_PROCESSED = "Joblet items processed",
		PREFIX_TASKS_PROCESSED = "Joblet tasks processed",
		PREFIX_FIRST_CREATED = "Joblet first created",
		PREFIX_NUM_SERVERS = "Joblet num servers",
		PREFIX_TARGET_SERVERS = "Joblet target servers";
	
	public static void incQueueLength(String key) {
		incQueueLength(key, 1L);
	}
	
	public static void incQueueLength(String key, long delta) {
		Counters.inc(PREFIX_QUEUE_LENGTH+" "+key, delta);
	}
	
	public static void incItemsProcessed(String key, long delta){
		Counters.inc(PREFIX_ITEMS_PROCESSED+" "+key, delta);
	}
	
	public static void incTasksProcessed(String key, long delta){
		Counters.inc(PREFIX_TASKS_PROCESSED+" "+key, delta);
	}
	
	public static void incFirstCreated(String key){
		incFirstCreated(key, 1L);
	}
	
	public static void incFirstCreated(String key, long delta){
		Counters.inc(PREFIX_FIRST_CREATED+" "+key, delta);
	}
	
	public static void incNumServers(long delta){
		Counters.inc(PREFIX_NUM_SERVERS, delta);
	}
	public static void incTargetServers(long delta){
		Counters.inc(PREFIX_TARGET_SERVERS, delta);
	}
}
