package com.hotpads.joblet;

import com.hotpads.datarouter.profile.counter.Counters;

public class JobletCounters {
	private static final String PREFIX = "Joblet ";

	public static void incQueueLength(String key){
		incQueueLength(key, 1L);
	}

	public static void incQueueLength(String key, long delta){
		Counters.inc(PREFIX + "queue length " + key, delta);
	}

	public static void incNumJobletsProcessed(){
		Counters.inc(PREFIX + "processed");
	}

	public static void incNumJobletsProcessed(String key){
		Counters.inc(PREFIX + "processed " + key);
	}

	public static void incItemsProcessed(String key, long delta){
		Counters.inc(PREFIX + "items processed " + key, delta);
	}

	public static void incTasksProcessed(String key, long delta){
		Counters.inc(PREFIX + "tasks processed " + key, delta);
	}

	public static void incFirstCreated(String key){
		incFirstCreated(key, 1L);
	}

	public static void incFirstCreated(String key, long delta){
		Counters.inc(PREFIX + "first created " + key, delta);
	}

	public static void incNumServers(long delta){
		Counters.inc(PREFIX + "num servers", delta);
	}

	public static void incTargetServers(long delta){
		Counters.inc(PREFIX + "target servers", delta);
	}

}
