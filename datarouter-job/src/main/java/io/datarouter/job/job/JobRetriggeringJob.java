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
package io.datarouter.job.job;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.TriggerGroupClasses;
import io.datarouter.job.scheduler.JobPackage;
import io.datarouter.job.scheduler.JobScheduler;
import io.datarouter.job.storage.joblock.DatarouterJobLockDao;
import io.datarouter.job.storage.joblock.JobLockKey;
import io.datarouter.tasktracker.scheduler.LongRunningTaskStatus;
import io.datarouter.tasktracker.service.LongRunningTaskService;
import io.datarouter.tasktracker.storage.LongRunningTask;
import io.datarouter.types.MilliTime;
import io.datarouter.util.Count;
import io.datarouter.util.Count.Counts;
import jakarta.inject.Inject;

public class JobRetriggeringJob extends BaseJob{
	private static final Logger logger = LoggerFactory.getLogger(JobRetriggeringJob.class);

	private static final Duration THRESHOLD = Duration.ofMinutes(30);

	@Inject
	private DatarouterInjector injector;
	@Inject
	private TriggerGroupClasses triggerGroupClasses;
	@Inject
	private JobScheduler jobScheduler;
	@Inject
	private LongRunningTaskService longRunningTaskService;
	@Inject
	private DatarouterJobLockDao clusterJobLockDao;

	private final Counts counts = new Counts();

	private final Count
			total = counts.add("total"),
			usesLocking = counts.add("usesLocking"),
			notRunningAgainSoon = counts.add("notRunningAgainSoon"),
			shouldRun = counts.add("shouldRun"),
			notLocked = counts.add("notLocked"),
			hasLastCompletionTime = counts.add("hasLastCompletionTime"),
			retriggered = counts.add("retriggered"),
			retriggerInterrupted = counts.add("retriggerInterrupted");

	@Override
	public void run(TaskTracker tracker){
		triggerGroupClasses.get()
				.concatIter(BaseTriggerGroup::getJobPackages)
				.each(total::increment)
				.include(JobPackage::usesLocking)
				.each(usesLocking::increment)
				.exclude(this::runningAgainSoon)// avoid LongRunningTask scan for frequent jobs
				.each(notRunningAgainSoon::increment)
				.include(JobPackage::shouldRun)
				.each(shouldRun::increment)
				.exclude(this::isLocked)
				.each(notLocked::increment)
				.forEach(jobPackage -> retriggerIfNecessary(jobPackage, tracker));
		logger.warn("{}", counts);
	}

	private boolean runningAgainSoon(JobPackage jobPackage){
		//look backwards a little to avoid jobs that are triggering at the same time as this JobRetriggeringJob
		Instant from = Instant.now().minus(Duration.ofSeconds(30));
		boolean runningAgainSoon = jobPackage.getNextValidTimeAfter(Date.from(from))
				.map(nextTrigger -> Duration.between(from, nextTrigger.toInstant()))
				.map(delay -> delay.compareTo(THRESHOLD) < 0)
				.orElse(true);
		logger.debug("job={} runningAgainSoon={}", jobPackage.jobClass.getSimpleName(), runningAgainSoon);
		return runningAgainSoon;
	}

	private boolean isLocked(JobPackage jobPackage){
		var key = new JobLockKey(jobPackage.jobClass.getSimpleName());
		boolean isLocked = clusterJobLockDao.exists(key);
		logger.debug("job={} isLocked={}", jobPackage.jobClass.getSimpleName(), isLocked);
		return isLocked;
	}

	private void retriggerIfNecessary(JobPackage jobPackage, TaskTracker tracker){
		String longRunningTaskName = injector.getInstance(jobPackage.jobClass).getPersistentName();
		Optional<LongRunningTask> lastNonRunningStatusTask = longRunningTaskService.findLastNonRunningStatusTask(
				longRunningTaskName);
		if(lastNonRunningStatusTask.isEmpty()){
			// if this job has only or no running tasks, it means it does not need to be retriggered.
			return;
		}
		if(lastNonRunningStatusTask.get().getJobExecutionStatus() == LongRunningTaskStatus.INTERRUPTED){
			logger.warn("job={} retriggerInterrupted", jobPackage.jobClass.getSimpleName());
			jobScheduler.scheduleRetriggeredJob(jobPackage, Instant.now());
			retriggerInterrupted.increment();
			tracker.increment();
			return;
		}
		Optional<MilliTime> lastSuccessCompletionTime = longRunningTaskService.findLastSuccess(longRunningTaskName)
				.map(MilliTime::of);
		logger.debug("job={} lastSuccessCompletionTime={}", jobPackage.jobClass.getSimpleName(),
				lastSuccessCompletionTime.orElse(null));
		if(lastSuccessCompletionTime.isEmpty()){
			return;
		}
		hasLastCompletionTime.increment();
		//getNextValidTimeAfter should be present, because only non-manual jobs get scheduled
		MilliTime testTriggerTime = jobPackage.getNextValidTimeAfter(lastSuccessCompletionTime.get()).get();
		MilliTime now = MilliTime.now();
		logger.debug("job={} nextTriggerTimeIsAfterNow={}", jobPackage.jobClass.getSimpleName(), testTriggerTime
				.isAfter(now));
		if(testTriggerTime.isAfter(now)){
			return;
		}

		//advance to the latest trigger time before the current date
		MilliTime triggerTime = testTriggerTime;
		while(testTriggerTime.isBefore(now)){
			testTriggerTime = jobPackage.getNextValidTimeAfter(testTriggerTime).get();
			if(testTriggerTime.isBefore(now)){
				triggerTime = testTriggerTime;
			}
		}
		jobScheduler.scheduleRetriggeredJob(jobPackage, triggerTime);
		retriggered.increment();
		tracker.increment();
	}

}
