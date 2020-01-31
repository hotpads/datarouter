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
package io.datarouter.joblet.job;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.joblet.enums.JobletPriority;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.queue.JobletRequestQueueManager;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.storage.jobletrequest.JobletRequestKey;
import io.datarouter.joblet.storage.jobletrequestqueue.DatarouterJobletQueueDao;
import io.datarouter.joblet.storage.jobletrequestqueue.JobletRequestQueueKey;
import io.datarouter.joblet.type.ActiveJobletTypeFactory;
import io.datarouter.util.tuple.Range;

/**
 * Find joblets that look to be stuck with status=created but apparently aren't in the queue, and requeue them.  If they
 * were already in the queue, then the extra message will be ignored when dequeued.
 */
public class JobletRequeueJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(JobletRequeueJob.class);

	private static final Duration
			MIDDLE_AGE = Duration.ofMinutes(30),
			OLD_AGE = Duration.ofHours(1);

	private static final int MAX_REQUEUES_PER_QUEUE = 10;

	@Inject
	private ActiveJobletTypeFactory activeJobletTypeFactory;
	@Inject
	private DatarouterJobletRequestDao jobletRequestDao;
	@Inject
	private JobletRequestQueueManager jobletRequestQueueManager;
	@Inject
	private DatarouterJobletQueueDao jobletQueueDao;

	@Override
	public void run(TaskTracker tracker){
		JobletRequestKey.createPrefixesForTypesAndPriorities(
				activeJobletTypeFactory.getAllActiveTypes(),
				EnumSet.allOf(JobletPriority.class))
				.advanceUntil($ -> tracker.shouldStop())
				.exclude(this::anyExistWithMediumAge)//wait for a gap between old-vs-new
				.forEach(this::requeueOld);
	}

	private boolean anyExistWithMediumAge(JobletRequestKey prefix){
		JobletRequestKey start = prefix.copy().withCreated(Instant.now().minus(OLD_AGE).toEpochMilli());
		JobletRequestKey end = prefix.copy().withCreated(Instant.now().minus(MIDDLE_AGE).toEpochMilli());
		var range = new Range<JobletRequestKey>(start, end);
		return jobletRequestDao.scan(range)
				.include(request -> request.getStatus() == JobletStatus.CREATED)
				.hasAny();
	}

	private void requeueOld(JobletRequestKey prefix){
		jobletRequestDao.scanWithPrefix(prefix)
				.include(request -> request.getStatus() == JobletStatus.CREATED)
				.advanceWhile(JobletRequeueJob::isOld)
				.limit(MAX_REQUEUES_PER_QUEUE)
				.forEach(this::requeue);
	}

	private void requeue(JobletRequest request){
		JobletRequestQueueKey queueKey = jobletRequestQueueManager.getQueueKey(request);
		JobletRequestKey oldKey = request.getKey().copy();//capture the old "created" for later delete
		request.getKey().setCreated(System.currentTimeMillis());
		jobletRequestDao.put(request);
		jobletQueueDao.put(queueKey, request);
		jobletRequestDao.delete(oldKey);
		logger.warn("requeued one, type={}, priority={}, age={}, jobletDataId={}",
				request.getKey().getType(),
				request.getKey().getPriority(),
				request.getKey().getAge(),
				request.getJobletDataId());
	}

	private static boolean isOld(JobletRequest request){
		return request.getKey().getAge().compareTo(OLD_AGE) > 0;
	}

}
