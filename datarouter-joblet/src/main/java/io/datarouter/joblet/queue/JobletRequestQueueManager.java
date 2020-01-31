/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.joblet.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.joblet.DatarouterJobletCounters;
import io.datarouter.joblet.enums.JobletPriority;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.storage.jobletrequest.JobletRequestKey;
import io.datarouter.joblet.storage.jobletrequestqueue.JobletRequestQueueKey;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletTypeFactory;

@Singleton
public class JobletRequestQueueManager{

	private static final long BACKOFF_MS = 2000;//skip queues that recently returned an empty result

	private final JobletTypeFactory jobletTypeFactory;
	private final DatarouterJobletCounters datarouterJobletCounters;

	private final ConcurrentMap<JobletRequestQueueKey,Long> lastMissByQueue;

	@Inject
	public JobletRequestQueueManager(
			JobletTypeFactory jobletTypeFactory,
			DatarouterJobletCounters datarouterJobletCounters){
		this.jobletTypeFactory = jobletTypeFactory;
		this.datarouterJobletCounters = datarouterJobletCounters;
		this.lastMissByQueue = new ConcurrentHashMap<>();
		getAllQueueKeys().forEach(key -> lastMissByQueue.put(key, 0L));
	}

	public JobletRequestQueueKey getQueueKey(JobletRequest jobletRequest){
		return getQueueKey(jobletRequest.getKey());
	}

	public JobletRequestQueueKey getQueueKey(JobletRequestKey jobletRequestKey){
		return new JobletRequestQueueKey(jobletTypeFactory.fromJobletRequestKey(jobletRequestKey), jobletRequestKey
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

	public void onJobletRequestMissForAllPriorities(JobletType<?> type){
		for(JobletPriority priority : JobletPriority.values()){
			JobletRequestQueueKey queueKey = new JobletRequestQueueKey(type, priority);
			onJobletRequestQueueMiss(queueKey);
		}
	}

	public void onJobletRequestQueueMiss(JobletRequestQueueKey queueKey){
		lastMissByQueue.put(queueKey, System.currentTimeMillis());
		datarouterJobletCounters.incQueueMiss(queueKey.getQueueName());
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
