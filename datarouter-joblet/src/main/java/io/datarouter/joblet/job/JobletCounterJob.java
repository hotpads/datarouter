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
import io.datarouter.joblet.DatarouterJobletCounters;
import io.datarouter.joblet.dto.JobletSummary;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import jakarta.inject.Inject;

public class JobletCounterJob extends BaseJob{

	@Inject
	private DatarouterJobletRequestDao jobletRequestDao;
	@Inject
	private DatarouterJobletCounters datarouterJobletCounters;

	@Override
	public void run(TaskTracker tracker){
		jobletRequestDao.scanAnyDelay()
				.advanceUntil($ -> tracker.increment().shouldStop())
				.map(JobletSummary::new)
				//aggregate by (status, type, queueId)
				.toMap(this::toStatusTypeQueueKey, Function.identity(), JobletSummary::absorbStats)
				.values().stream()
				.peek(this::saveQueueStatsByStatusTypeAndQueueId)
				//aggregate by (status, type)
				.collect(Collectors.toMap(this::toStatusTypeKey, Function.identity(), JobletSummary::absorbStats))
				.values().stream()
				.peek(this::saveQueueStatsByStatusAndType)
				//aggregate by (status)
				.collect(Collectors.toMap(JobletSummary::getStatus, Function.identity(), JobletSummary::absorbStats))
				.values().stream()
				.forEach(this::saveQueueStatsByStatus);
	}

	private void saveQueueStatsByStatus(JobletSummary summary){
		JobletStatus status = summary.getStatus();
		datarouterJobletCounters.saveGlobalQueueLengthJoblets(status, summary.getNumType());
		datarouterJobletCounters.saveGlobalQueueLengthItems(status, summary.getSumItems());
		datarouterJobletCounters.saveGlobalFirst(status, getFirstCreatedMinutesToNow(summary));
	}

	private void saveQueueStatsByStatusAndType(JobletSummary summary){
		JobletStatus status = summary.getStatus();
		String jobletType = summary.getType();
		datarouterJobletCounters.saveQueueLengthJoblets(status, jobletType, summary.getNumType());
		datarouterJobletCounters.saveQueueLengthItems(status, jobletType, summary.getSumItems());
		datarouterJobletCounters.saveFirst(status, jobletType, getFirstCreatedMinutesToNow(summary));
	}

	private void saveQueueStatsByStatusTypeAndQueueId(JobletSummary summary){
		JobletStatus status = summary.getStatus();
		String jobletType = summary.getType();
		String queueId = summary.getQueueId();
		datarouterJobletCounters.saveQueueLengthJobletsForQueueId(status, jobletType, queueId, summary.getNumType());
		datarouterJobletCounters.saveQueueLengthItemsForQueueId(status, jobletType, queueId, summary.getSumItems());
		datarouterJobletCounters.saveFirstForQueueId(status, jobletType, queueId, getFirstCreatedMinutesToNow(summary));
	}

	private StatusAndTypeKey toStatusTypeKey(JobletSummary summary){
		return new StatusAndTypeKey(summary.getStatus(), summary.getType());
	}

	private JobletStatusAndTypeAndQueueId toStatusTypeQueueKey(JobletSummary summary){
		return new JobletStatusAndTypeAndQueueId(summary.getStatus(), summary.getType(), summary.getQueueId());
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

	private Long getFirstCreatedMinutesToNow(JobletSummary summary){
		return summary.getFirstCreated().until(Instant.now(), ChronoUnit.MINUTES);
	}

}
