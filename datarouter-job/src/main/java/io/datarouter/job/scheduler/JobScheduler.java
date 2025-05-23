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
package io.datarouter.job.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.job.BaseJob;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.JobCounters;
import io.datarouter.job.LocalJobProcessor;
import io.datarouter.job.config.DatarouterJobExecutors.DatarouterJobScheduler;
import io.datarouter.job.config.DatarouterJobSettingRoot;
import io.datarouter.job.detached.DetachedJobExecutor.DetachedJobExecutorSupplier;
import io.datarouter.job.lock.LocalTriggerLockService;
import io.datarouter.job.lock.TriggerLockConfig;
import io.datarouter.job.lock.TriggerLockService;
import io.datarouter.job.scheduler.JobWrapper.JobWrapperFactory;
import io.datarouter.job.util.DatarouterJobOutcome;
import io.datarouter.types.MilliTime;
import io.datarouter.util.concurrent.ThreadTool;
import io.datarouter.util.number.RandomTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Provides scheduling, shouldRun, local and cluster locking for jobs.
 */
@Singleton
public class JobScheduler{
	private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

	private static final Duration LOCK_ACQUISITION_DELAY_INCREMENT = Duration.ofMillis(100);

	public static final long JOB_STOP_GRACE_PERIOD_MS = Duration.ofSeconds(5L).toMillis();

	private final DatarouterJobScheduler triggerExecutor;
	private final DetachedJobExecutorSupplier detachedJobExecutor;
	private final DatarouterJobSettingRoot jobSettings;
	private final JobCategoryTracker jobCategoryTracker;
	private final JobPackageTracker jobPackageTracker;
	private final LocalJobProcessor localJobProcessor;
	private final LocalTriggerLockService localTriggerLockService;
	private final TriggerLockService triggerLockService;
	private final JobWrapperFactory jobWrapperFactory;
	private final AtomicBoolean shutdown;

	@Inject
	public JobScheduler(
			DatarouterJobScheduler triggerExecutor,
			DetachedJobExecutorSupplier detachedJobExecutor,
			DatarouterJobSettingRoot jobSettings,
			JobCategoryTracker jobCategoryTracker,
			JobPackageTracker jobPackageTracker,
			LocalJobProcessor localJobProcessor,
			LocalTriggerLockService localTriggerLockService,
			TriggerLockService triggerLockService,
			JobWrapperFactory jobWrapperFactory){
		this.triggerExecutor = triggerExecutor;
		this.detachedJobExecutor = detachedJobExecutor;
		this.jobSettings = jobSettings;
		this.jobCategoryTracker = jobCategoryTracker;
		this.jobPackageTracker = jobPackageTracker;
		this.localJobProcessor = localJobProcessor;
		this.localTriggerLockService = localTriggerLockService;
		this.triggerLockService = triggerLockService;
		this.jobWrapperFactory = jobWrapperFactory;
		this.shutdown = new AtomicBoolean();
	}

	public void registerTriggers(BaseTriggerGroup triggerGroup){
		triggerGroup.getJobPackages().forEach(this::register);
	}

	public DatarouterJobOutcome triggerManualJob(
			Class<? extends BaseJob> jobClass,
			String triggeredBy){
		JobPackage jobPackage = JobPackage.createManualFromScheduledPackage(jobPackageTracker.getForClass(jobClass));
		return triggerManualJob(jobClass, jobPackage, triggeredBy, jobPackage.shouldRunDetached);
	}

	public DatarouterJobOutcome triggerManualJob(
			Class<? extends BaseJob> jobClass,
			String triggeredBy,
			boolean runDetached){
		JobPackage jobPackage = JobPackage.createManualFromScheduledPackage(jobPackageTracker.getForClass(jobClass));
		return triggerManualJob(jobClass, jobPackage, triggeredBy, runDetached);
	}

	private DatarouterJobOutcome triggerManualJob(
			Class<? extends BaseJob> jobClass,
			JobPackage jobPackage,
			String triggeredBy,
			boolean runDetached){
		JobWrapper jobWrapper = jobWrapperFactory.createManual(jobPackage, jobClass, triggeredBy);
		return triggerManual(jobWrapper, runDetached);
	}

	public void shutdown(){
		shutdown.set(true); // stop scheduling new jobs
		localTriggerLockService.onShutdown(); // tell currently running jobs to stop
		triggerExecutor.shutdown(); // start rejecting new triggers
		ThreadTool.sleepUnchecked(JOB_STOP_GRACE_PERIOD_MS); // give some time for currently running jobs to stop
		localJobProcessor.shutdown(); // interrupt all jobs
		triggerExecutor.shutdownNow(); // interrupt all triggers
		releaseThisServersActiveTriggerLocks();
		triggerLockService.releaseThisServersJobLocks();
	}

	/*-------------- schedule ----------------*/

	private void scheduleNextRun(JobPackage jobPackage){
		Instant now = Instant.now();
		Optional<Date> nextValidTimeAfter = jobPackage.getNextValidTimeAfter(Date.from(now));
		if(nextValidTimeAfter.isEmpty()){
			logger.warn("couldn't schedule {} because no trigger defined", getClass());
			return;
		}
		Instant nextTriggerTime = nextValidTimeAfter.get().toInstant();
		Duration durationUntilNextTrigger = Duration.between(now, nextTriggerTime);
		JobWrapper jobWrapper = jobWrapperFactory.createScheduled(
				jobPackage,
				jobPackage.jobClass,
				nextTriggerTime,
				nextTriggerTime,
				getClass().getSimpleName());
		schedule(jobWrapper, durationUntilNextTrigger.toMillis(), false, false);
	}

	public void scheduleRetriggeredJob(JobPackage jobPackage, MilliTime officialTriggerTime){
		scheduleRetriggeredJob(jobPackage, officialTriggerTime.toInstant());
	}

	public void scheduleRetriggeredJob(JobPackage jobPackage, Instant officialTriggerTime){
		logger.warn(
				"retriggering {} with official triggerTime {} to run immediately",
				jobPackage.jobClass.getSimpleName(),
				TriggerLockService.formatTime(officialTriggerTime));
		JobWrapper jobWrapper = jobWrapperFactory.createRetriggered(
				jobPackage,
				jobPackage.jobClass,
				officialTriggerTime,
				Instant.now(),
				getClass().getSimpleName() + " JobRetriggeringJob");
		schedule(jobWrapper, 0, true, true);
	}

	private void schedule(JobWrapper jobWrapper, long delayMs, boolean logIfRan, boolean logIfDidNotRun){
		if(shutdown.get()){
			logger.warn("Job scheduler is shutdown, not scheduling {}", jobWrapper.jobClass.getSimpleName());
			return;
		}
		triggerExecutor.schedule(
				() -> triggerScheduled(jobWrapper, logIfRan, logIfDidNotRun),
				delayMs,
				TimeUnit.MILLISECONDS);
	}

	/*-------------- trigger/run ----------------*/

	private void triggerScheduled(JobWrapper jobWrapper, boolean logIfRan, boolean logIfDidNotRun){
		if(shutdown.get()){
			logger.warn("Job scheduler is shutdown, not running {}", jobWrapper.jobClass.getSimpleName());
			return;
		}
		Class<? extends BaseJob> jobClass = jobWrapper.jobClass;
		JobPackage jobPackage = jobWrapper.jobPackage;
		try{
			if(!configuredToRun(jobPackage)){
				return;
			}
			DatarouterJobOutcome didRun;
			if(jobPackage.shouldRunDetached && jobSettings.enableDetachedJobs.get()){
				didRun = runDetached(jobWrapper);
			}else if(jobPackage.usesLocking()){
				Duration delay = delayLockAquisitionBasedOnCurrentWorkload();
				didRun = tryAcquireClusterLockAndRun(jobWrapper, jobPackage.triggerLockConfig, delay);
			}else{
				didRun = tryAcquireLocalLockAndRun(jobWrapper);
			}
			if(logIfRan && didRun.success()){
				logger.warn("{} did run", jobClass.getName());
			}
			if(logIfDidNotRun && didRun.failed()){
				logger.warn("{} did not run, reason={}", jobClass.getName(), didRun.reason());
			}
		}catch(Exception e){
			JobCounters.exception(jobClass);
			logger.warn("exception jobName={}", jobClass.getName(), e);
		}finally{
			try{
				if(jobWrapper.reschedule){
					scheduleNextRun(jobPackage);
				}
			}catch(Exception e){
				logger.error("exception scheduling next run for {}", jobClass, e);
			}
		}
	}

	private DatarouterJobOutcome triggerManual(JobWrapper jobWrapper, boolean runDetached){
		if(runDetached){
			return runDetached(jobWrapper);
		}
		JobPackage jobPackage = jobWrapper.jobPackage;
		return jobPackage.usesLocking()
				? tryAcquireClusterLockAndRun(jobWrapper, jobPackage.triggerLockConfig, Duration.ZERO)
				: tryAcquireLocalLockAndRun(jobWrapper);
	}

	private Duration delayLockAquisitionBasedOnCurrentWorkload(){
		long delayIncrementMs = LOCK_ACQUISITION_DELAY_INCREMENT.toMillis();
		long workloadDelayMs = delayIncrementMs * localTriggerLockService.getNumRunningJobs();
		//reduce the advantage of servers with a lower latency connection to the lock server
		long randomExtraDelayMs = RandomTool.nextPositiveLong(delayIncrementMs);
		long totalDelayMs = workloadDelayMs + randomExtraDelayMs;
		ThreadTool.trySleep(totalDelayMs);
		return Duration.ofMillis(totalDelayMs);
	}

	private DatarouterJobOutcome tryAcquireClusterLockAndRun(
			JobWrapper jobWrapper,
			TriggerLockConfig triggerLockConfig,
			Duration delay){
		var jobAndTriggerLocksAcquired = triggerLockService.acquireJobAndTriggerLocks(
				triggerLockConfig,
				jobWrapper.triggerTime,
				delay);
		if(jobAndTriggerLocksAcquired.failed()){
			return jobAndTriggerLocksAcquired;
		}
		try{
			var started = tryAcquireLocalLockAndRun(jobWrapper);
			if(started.failed()){
				triggerLockService.tryReleasingJobAndTriggerLocks(triggerLockConfig, jobWrapper.triggerTime);
				return started;
			}
		}catch(Exception e){
			triggerLockService.tryReleasingJobAndTriggerLocks(triggerLockConfig, jobWrapper.triggerTime);
			throw e;
		}
		// On success release jobLock but keep clusterTriggerLock
		try{
			triggerLockService.releaseJobLock(triggerLockConfig.jobName);
		}catch(Exception e){
			logger.warn("failed to release jobLock for {}", triggerLockConfig.jobName, e);
		}
		return DatarouterJobOutcome.makeSuccess();
	}

	private DatarouterJobOutcome tryAcquireLocalLockAndRun(JobWrapper jobWrapper){
		DatarouterJobOutcome localLockAcquired = localTriggerLockService.acquire(jobWrapper);
		if(localLockAcquired.failed()){
			return localLockAcquired;
		}
		try{
			return localJobProcessor.run(jobWrapper);
		}finally{
			localTriggerLockService.release(jobWrapper.jobClass);
		}
	}

	private DatarouterJobOutcome runDetached(JobWrapper jobWrapper){
		JobPackage jobPackage = jobWrapper.jobPackage;
		DatarouterJobOutcome jobAndTriggerLocksAcquired = triggerLockService.acquireJobAndTriggerLocks(
				jobPackage.triggerLockConfig,
				jobWrapper.triggerTime,
				Duration.ZERO);
		if(jobAndTriggerLocksAcquired.failed()){
			return jobAndTriggerLocksAcquired;
		}
		try{
			detachedJobExecutor.get().submit(jobWrapper);
			// Not releasing the JobLock here, 'ownership' transferred to the detached job
		}catch(Exception e){
			triggerLockService.tryReleasingJobAndTriggerLocks(
					jobWrapper.jobPackage.triggerLockConfig,
					jobWrapper.triggerTime);
			throw e;
		}
		return DatarouterJobOutcome.makeSuccess();
	}

	/*---------------- helpers -------------------*/

	private void register(JobPackage jobPackage){
		jobPackageTracker.register(jobPackage);
		jobCategoryTracker.register(jobPackage.jobCategoryName);
		scheduleNextRun(jobPackage);
	}

	private boolean configuredToRun(JobPackage jobPackage){
		if(!jobSettings.processJobs.get()){
			return false;
		}
		return jobPackage.shouldRun();
	}

	private void releaseThisServersActiveTriggerLocks(){
		localTriggerLockService.getJobWrappers().forEach(jobWrapper -> {
			String jobName = BaseTriggerGroup.lockName(jobWrapper.jobClass);
			Instant triggerTime = jobWrapper.triggerTime;
			triggerLockService.releaseTriggerLock(jobName, triggerTime);
		});
	}

}
