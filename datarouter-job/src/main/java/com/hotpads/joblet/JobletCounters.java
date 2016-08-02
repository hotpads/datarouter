package com.hotpads.joblet;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.profile.counter.Counters;
import com.hotpads.datarouter.profile.metrics.Metrics;

@Singleton
public class JobletCounters {
	private static final String PREFIX = "Joblet ";

	@Inject
	private Metrics metrics;

	public void saveQueueLength(String key, long queueLength){
		metrics.save(PREFIX + "queue length " + key, queueLength);
	}

	public void saveFirstCreated(String key, long firstCreated){
		metrics.save(PREFIX + "first created " + key, firstCreated);
	}

	public void saveNumServers(long numServers){
		metrics.save(PREFIX + "num servers", numServers);
	}

	public void saveTargetServers(long numTargetServers){
		metrics.save(PREFIX + "target servers", numTargetServers);
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

}
