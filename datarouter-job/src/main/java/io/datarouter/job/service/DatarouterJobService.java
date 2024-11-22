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
package io.datarouter.job.service;

import java.time.Instant;
import java.util.Optional;

import org.apache.logging.log4j.core.util.CronExpression;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.TriggerGroupClasses;
import io.datarouter.job.scheduler.JobPackage;
import io.datarouter.tasktracker.service.LongRunningTaskTracker;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterJobService{

	@Inject
	private TriggerGroupClasses triggerGroupClasses;

	public Optional<CronExpression> findCronExpression(TaskTracker tracker){
		return findJobPackage(tracker)
				.flatMap(JobPackage::getCronExpression);
	}

	public Optional<Boolean> findIsDetached(TaskTracker tracker){
		return findJobPackage(tracker)
				.map(jp -> jp.shouldRunDetached);
	}

	private Optional<JobPackage> findJobPackage(TaskTracker tracker){
		String jobName = tracker.getName();
		return triggerGroupClasses.get()
				.concatIter(BaseTriggerGroup::getJobPackages)
				.include(jp -> jp.jobClass.getSimpleName().equals(jobName))
				.findFirst();
	}

	public Optional<Instant> findScheduledTime(TaskTracker tracker){
		return LongRunningTaskTracker.findFromTaskTracker(tracker)
				.map(LongRunningTaskTracker::getTriggerTime);
	}

	public Optional<String> findExceptionRecordId(TaskTracker tracker){
		return LongRunningTaskTracker.findFromTaskTracker(tracker)
				.map(LongRunningTaskTracker::getExceptionRecordId);
	}

}
