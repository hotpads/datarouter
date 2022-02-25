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
package io.datarouter.job.monitoring;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.TriggerGroupClasses;
import io.datarouter.job.scheduler.JobPackage;
import io.datarouter.job.scheduler.JobScheduler;
import io.datarouter.job.storage.clusterjoblock.ClusterJobLockKey;
import io.datarouter.job.storage.clusterjoblock.DatarouterClusterJobLockDao;
import io.datarouter.tasktracker.service.LongRunningTaskService;
import io.datarouter.util.Count;
import io.datarouter.util.Count.Counts;

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
	private DatarouterClusterJobLockDao clusterJobLockDao;

	private final Counts counts = new Counts();

	private final Count
			total = counts.add("total"),
			usesLocking = counts.add("usesLocking"),
			notRunningAgainSoon = counts.add("notRunningAgainSoon"),
			shouldRun = counts.add("shouldRun"),
			notLocked = counts.add("notLocked"),
			hasLastCompletionTime = counts.add("hasLastCompletionTime"),
			retriggered = counts.add("retriggered");

	@Override
	public void run(TaskTracker tracker){
		triggerGroupClasses.get().stream()
				.map(BaseTriggerGroup::getJobPackages)
				.flatMap(Collection::stream)
				.peek(total::increment)
				.filter(JobPackage::usesLocking)
				.peek(usesLocking::increment)
				.filter(Predicate.not(this::runningAgainSoon))// avoid LongRunningTask scan for frequent jobs
				.peek(notRunningAgainSoon::increment)
				.filter(JobPackage::shouldRun)
				.peek(shouldRun::increment)
				.filter(Predicate.not(this::isLocked))
				.peek(notLocked::increment)
				.forEach(jobPackage -> retriggerIfNecessary(jobPackage, tracker));
		logger.warn(counts.toString());
	}

	private boolean runningAgainSoon(JobPackage jobPackage){
		//look backwards a little to avoid jobs that are triggering at the same time as this JobRetriggeringJob
		Instant from = Instant.now().minus(Duration.ofSeconds(30));
		return jobPackage.getNextValidTimeAfter(Date.from(from))
				.map(nextTrigger -> Duration.between(from, nextTrigger.toInstant()))
				.map(delay -> delay.compareTo(THRESHOLD) < 0)
				.orElse(true);
	}

	private boolean isLocked(JobPackage jobPackage){
		var key = new ClusterJobLockKey(jobPackage.jobClass.getSimpleName());
		return clusterJobLockDao.exists(key);
	}

	private void retriggerIfNecessary(JobPackage jobPackage, TaskTracker tracker){
		String longRunningTaskName = injector.getInstance(jobPackage.jobClass).getPersistentName();
		Optional<Instant> lastCompletionTime = longRunningTaskService.findLastSuccessInstant(longRunningTaskName);
		if(lastCompletionTime.isEmpty()){
			return;
		}
		hasLastCompletionTime.increment();

		//getNextValidTimeAfter should be present, because only non-manual jobs get scheduled
		Instant testTriggerTime = jobPackage.getNextValidTimeAfter(lastCompletionTime.get()).get();
		Instant now = Instant.now();
		if(testTriggerTime.isAfter(now)){
			return;
		}

		//advance to the latest trigger time before the current date
		Instant triggerTime = testTriggerTime;
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
