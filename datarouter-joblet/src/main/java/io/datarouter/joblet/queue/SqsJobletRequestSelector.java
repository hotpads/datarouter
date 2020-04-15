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

import java.time.Duration;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.joblet.DatarouterJobletConstants;
import io.datarouter.joblet.DatarouterJobletCounters;
import io.datarouter.joblet.enums.JobletPriority;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.storage.jobletrequest.JobletRequestKey;
import io.datarouter.joblet.storage.jobletrequestqueue.DatarouterJobletQueueDao;
import io.datarouter.joblet.storage.jobletrequestqueue.JobletRequestQueueKey;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.util.timer.PhaseTimer;

public class SqsJobletRequestSelector implements JobletRequestSelector{
	private static final Logger logger = LoggerFactory.getLogger(SqsJobletRequestSelector.class);

	@Inject
	private DatarouterJobletRequestDao jobletRequestDao;
	@Inject
	private JobletRequestQueueManager jobletRequestQueueManager;
	@Inject
	private DatarouterJobletCounters datarouterJobletCounters;
	@Inject
	private DatarouterJobletQueueDao jobletQueueDao;

	@Override
	public Optional<JobletRequest> getJobletRequestForProcessing(
			PhaseTimer timer,
			JobletType<?> type,
			String reservedBy){
		for(JobletPriority priority : JobletPriority.values()){
			JobletRequestQueueKey queueKey = new JobletRequestQueueKey(type, priority);
			if(jobletRequestQueueManager.shouldSkipQueue(queueKey)){
				datarouterJobletCounters.incQueueSkip(queueKey.getQueueName());
				continue;
			}
			// set timeout to 0 so we return immediately. processor threads can do the waiting
			Config config = new Config()
					.setTimeout(Duration.ofMillis(0))
					.setVisibilityTimeoutMs(DatarouterJobletConstants.RUNNING_JOBLET_TIMEOUT_MS);
			QueueMessage<JobletRequestKey,JobletRequest> message = jobletQueueDao.getQueue(queueKey).peek(config);
			timer.add("peek");
			if(message == null){
				jobletRequestQueueManager.onJobletRequestQueueMiss(queueKey);
				continue;
			}
			datarouterJobletCounters.incQueueHit(queueKey.getQueueName());
			JobletRequest jobletRequest = message.getDatabean();
			boolean existsInDb = jobletRequestDao.exists(jobletRequest.getKey());
			timer.add("check exists");
			if(!existsInDb){
				logger.warn("draining non-existent JobletRequest without processing jobletRequest={}", jobletRequest);
				datarouterJobletCounters.ignoredRequestMissingFromDb(type);
				jobletQueueDao.getQueue(queueKey).ack(message.getKey());
				timer.add("ack missing request");
				continue;
			}
			jobletRequest.setQueueMessageKey(message.getKey());
			jobletRequest.setReservedBy(reservedBy);
			jobletRequest.setReservedAt(System.currentTimeMillis());
			jobletRequest.setStatus(JobletStatus.RUNNING);
			jobletRequestDao.put(jobletRequest);
			if(!jobletRequest.getRestartable()){//don't let SQS give this joblet out again
				jobletQueueDao.getQueue(queueKey).ack(message.getKey());
				timer.add("ack non-restartable");
			}
			return Optional.of(jobletRequest);
		}
		return Optional.empty();
	}

}
