package com.hotpads.joblet.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletRequestKey;
import com.hotpads.joblet.enums.JobletPriority;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;

@Singleton
public class JobletRequestQueueManager{

	private static final long BACKOFF_MS = 2000;//skip queues that recently returned an empty result

	//injected
	private final JobletTypeFactory jobletTypeFactory;

	private final ConcurrentMap<JobletRequestQueueKey,Long> lastMissByQueue;

	@Inject
	public JobletRequestQueueManager(JobletTypeFactory jobletTypeFactory){
		this.jobletTypeFactory = jobletTypeFactory;
		this.lastMissByQueue = new ConcurrentHashMap<>();
		getAllQueueKeys().forEach(key -> lastMissByQueue.put(key, 0L));
	}

	//TODO rename from JobletRequest
	public JobletRequestQueueKey getQueueKey(JobletRequest jobletRequest){
		return getQueueKey(jobletRequest.getKey());
	}

	//TODO rename from JobletRequestKey
	public JobletRequestQueueKey getQueueKey(JobletRequestKey jobletRequestKey){
		return new JobletRequestQueueKey(jobletTypeFactory.fromJobletKey(jobletRequestKey), jobletRequestKey
				.getPriority());
	}

	public List<JobletRequestQueueKey> getAllQueueKeys(){
		List<JobletRequestQueueKey> queueKeys = new ArrayList<>();
		for(JobletType<?> type : jobletTypeFactory.getAllTypes()){
			for(JobletPriority priority : JobletPriority.values()){
				queueKeys.add(new JobletRequestQueueKey(type, priority));
			}
		}
		return queueKeys;
	}

	public void onJobletRequestQueueMiss(JobletRequestQueueKey queueKey){
		lastMissByQueue.put(queueKey, System.currentTimeMillis());
	}

	public boolean shouldSkipQueue(JobletRequestQueueKey queueKey){
		long lastMissAgoMs = System.currentTimeMillis() - lastMissByQueue.get(queueKey);
		return lastMissAgoMs < BACKOFF_MS;
	}

	public Optional<JobletRequestQueueKey> getQueueToCheck(JobletType<?> jobletType){
		for(JobletPriority priority : JobletPriority.values()){
			JobletRequestQueueKey queueKey = new JobletRequestQueueKey(jobletType, priority);
			if(!shouldSkipQueue(queueKey)){
				return Optional.of(queueKey);
			}
		}
		return Optional.empty();
	}

	public boolean shouldCheckAnyQueues(JobletType<?> jobletType){
		return getQueueToCheck(jobletType).isPresent();
	}

}
