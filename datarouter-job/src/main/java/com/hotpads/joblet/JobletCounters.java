package com.hotpads.joblet;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.profile.counter.Counters;
import com.hotpads.datarouter.profile.metrics.Metrics;
import com.hotpads.joblet.enums.JobletType;

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

	public void incNumJobletsInserted(){
		Counters.inc(PREFIX + "inserted");
	}

	public void incNumJobletsInserted(JobletType<?> jobletType){
		Counters.inc(PREFIX + "inserted " + jobletType.getPersistentString());
	}

	public void incNumJobletsProcessed(){
		Counters.inc(PREFIX + "processed");
	}

	public void incNumJobletsProcessed(JobletType<?> jobletType){
		Counters.inc(PREFIX + "processed " + jobletType.getPersistentString());
	}

	public void incItemsProcessed(JobletType<?> jobletType, long delta){
		Counters.inc(PREFIX + "items processed " + jobletType.getPersistentString(), delta);
	}

	public void incTasksProcessed(JobletType<?> jobletType, long delta){
		Counters.inc(PREFIX + "tasks processed " + jobletType.getPersistentString(), delta);
	}

	public void incQueueSkip(String key){
		Counters.inc(PREFIX + "queue " + key + " skip");
	}

	public void incQueueHit(String key){
		Counters.inc(PREFIX + "queue " + key + " hit");
	}

	public void incQueueMiss(String key){
		Counters.inc(PREFIX + "queue " + key + " miss");
	}

	public void rejectedCallable(JobletType<?> jobletType){
		Counters.inc(PREFIX + "rejected callable " + jobletType.getPersistentString());
	}

	public void ignoredRequestMissingFromDb(JobletType<?> jobletType){
		Counters.inc(PREFIX + "ignored request missing from db " + jobletType.getPersistentString());
	}

}
