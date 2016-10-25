package com.hotpads.joblet.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.enums.JobletPriority;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;

@Singleton
public class JobletQueueManager{

	private static final long TIMEOUT_MS = 2000;

	//injected
	private final JobletTypeFactory jobletTypeFactory;

	private final ConcurrentMap<JobletRequestQueueKey,Long> lastDequeueByQueue;

	@Inject
	public JobletQueueManager(JobletTypeFactory jobletTypeFactory){
		this.jobletTypeFactory = jobletTypeFactory;
		this.lastDequeueByQueue = new ConcurrentHashMap<>();
		getAllQueueKeys().forEach(key -> lastDequeueByQueue.put(key, 0L));
	}


	public JobletRequestQueueKey getQueueKey(JobletRequest jobletRequest){
		return new JobletRequestQueueKey(jobletTypeFactory.fromJobletRequest(jobletRequest), jobletRequest.getKey()
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

	public void onJobletRequestFound(JobletRequestQueueKey queueKey){
		lastDequeueByQueue.put(queueKey, System.currentTimeMillis());
	}

	public boolean shouldSkipQueue(JobletRequestQueueKey queueKey){
		return lastDequeueByQueue.get(queueKey) < TIMEOUT_MS;
	}


}
