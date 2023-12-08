/*
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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.datarouter.joblet.JobletCounters;
import io.datarouter.joblet.enums.JobletPriority;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.storage.jobletrequest.JobletRequestKey;
import io.datarouter.joblet.storage.jobletrequestqueue.JobletRequestQueueKey;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletTypeFactory;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.WarnOnModifyList;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JobletRequestQueueManager{

	private final JobletTypeFactory jobletTypeFactory;
	private final List<JobletRequestQueueKey> queueKeys;
	private final ConcurrentMap<JobletRequestQueueKey,Long> lastMissByQueue;

	@Inject
	public JobletRequestQueueManager(JobletTypeFactory jobletTypeFactory){
		this.jobletTypeFactory = jobletTypeFactory;
		this.lastMissByQueue = new ConcurrentHashMap<>();
		queueKeys = Scanner.of(jobletTypeFactory.getAllTypes())
				.concat(type -> Scanner.of(JobletPriority.values())
						.map(priority -> new JobletRequestQueueKey(type, priority)))
				.collect(WarnOnModifyList.deprecatedCollector());
		queueKeys.forEach(key -> lastMissByQueue.put(key, 0L));
	}

	public JobletRequestQueueKey getQueueKey(JobletRequest jobletRequest){
		return getQueueKey(jobletRequest.getKey());
	}

	public JobletRequestQueueKey getQueueKey(JobletRequestKey jobletRequestKey){
		return new JobletRequestQueueKey(
				jobletTypeFactory.fromJobletRequestKey(jobletRequestKey),
				jobletRequestKey.getPriority());
	}

	public void onJobletRequestMissForAllPriorities(JobletType<?> type){
		Scanner.of(JobletPriority.values())
				.map(priority -> new JobletRequestQueueKey(type, priority))
				.forEach(this::onJobletRequestQueueMiss);
	}

	public void onJobletRequestQueueMiss(JobletRequestQueueKey queueKey){
		lastMissByQueue.put(queueKey, System.currentTimeMillis());
		JobletCounters.incQueueMiss(queueKey.getQueueName());
	}

	public boolean shouldSkipQueue(JobletRequestQueueKey queueKey){
		long lastMissAgoMs = System.currentTimeMillis() - lastMissByQueue.get(queueKey);
		return lastMissAgoMs < queueKey.type().pollingPeriod.toMillis();
	}

	public Optional<JobletRequestQueueKey> findQueueToCheck(JobletType<?> jobletType){
		return Scanner.of(JobletPriority.values())
				.map(priority -> new JobletRequestQueueKey(jobletType, priority))
				.exclude(this::shouldSkipQueue)
				.findFirst();
	}

	public boolean shouldCheckAnyQueues(JobletType<?> jobletType){
		return findQueueToCheck(jobletType).isPresent();
	}

	public List<JobletRequestQueueKey> getQueueKeys(){
		return queueKeys;
	}

}
