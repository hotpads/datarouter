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
package io.datarouter.joblet.job;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.joblet.JobletCounters;
import io.datarouter.joblet.dto.JobletSummary;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import jakarta.inject.Inject;

public class JobletCounterJob extends BaseJob{

	@Inject
	private DatarouterJobletRequestDao jobletRequestDao;

	@Override
	public void run(TaskTracker tracker){
		jobletRequestDao.scanAnyDelay()
				.advanceUntil($ -> tracker.increment().shouldStop())
				.map(JobletSummary::new)
				//aggregate by (status, type, queueId)
				.toMap(JobletCounterJob::toStatusTypeQueueKey, Function.identity(), JobletSummary::absorbStats)
				.values().stream()
				.peek(JobletCounterJob::saveQueueStatsByStatusTypeAndQueueId)
				//aggregate by (status, type)
				.collect(Collectors.toMap(JobletCounterJob::toStatusTypeKey, Function.identity(),
						JobletSummary::absorbStats))
				.values().stream()
				.peek(JobletCounterJob::saveQueueStatsByStatusAndType)
				//aggregate by (status)
				.collect(Collectors.toMap(JobletSummary::getStatus, Function.identity(), JobletSummary::absorbStats))
				.values()
				.forEach(JobletCounterJob::saveQueueStatsByStatus);
	}

	private static void saveQueueStatsByStatus(JobletSummary summary){
		JobletStatus status = summary.getStatus();
		JobletCounters.saveGlobalQueueLengthJoblets(status, summary.getNumType());
		JobletCounters.saveGlobalQueueLengthItems(status, summary.getSumItems());
		JobletCounters.saveGlobalFirst(status, getFirstCreatedMinutesToNow(summary));
	}

	private static void saveQueueStatsByStatusAndType(JobletSummary summary){
		JobletStatus status = summary.getStatus();
		String jobletType = summary.getType();
		JobletCounters.saveQueueLengthJoblets(status, jobletType, summary.getNumType());
		JobletCounters.saveQueueLengthItems(status, jobletType, summary.getSumItems());
		JobletCounters.saveFirst(status, jobletType, getFirstCreatedMinutesToNow(summary));
	}

	private static void saveQueueStatsByStatusTypeAndQueueId(JobletSummary summary){
		JobletStatus status = summary.getStatus();
		String jobletType = summary.getType();
		String queueId = summary.getQueueId();
		JobletCounters.saveQueueLengthJobletsForQueueId(status, jobletType, queueId, summary.getNumType());
		JobletCounters.saveQueueLengthItemsForQueueId(status, jobletType, queueId, summary.getSumItems());
		JobletCounters.saveFirstForQueueId(status, jobletType, queueId, getFirstCreatedMinutesToNow(summary));
	}

	private static StatusAndTypeKey toStatusTypeKey(JobletSummary summary){
		return new StatusAndTypeKey(summary.getStatus(), summary.getType());
	}

	private static JobletStatusAndTypeAndQueueId toStatusTypeQueueKey(JobletSummary summary){
		return new JobletStatusAndTypeAndQueueId(summary.getStatus(), summary.getType(), summary.getQueueId());
	}

	private static Long getFirstCreatedMinutesToNow(JobletSummary summary){
		return summary.getFirstCreated().until(Instant.now(), ChronoUnit.MINUTES);
	}

	private record StatusAndTypeKey(
			JobletStatus status,
			String type){
	}

	private record JobletStatusAndTypeAndQueueId(
			JobletStatus status,
			String type,
			String queueId){
	}

}
