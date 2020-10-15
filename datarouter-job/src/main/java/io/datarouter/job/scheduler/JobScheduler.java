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
package io.datarouter.job.scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.job.BaseJob;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.JobCounters;
import io.datarouter.job.JobExceptionCategory;
import io.datarouter.job.config.DatarouterJobExecutors.DatarouterJobExecutor;
import io.datarouter.job.config.DatarouterJobExecutors.DatarouterJobScheduler;
import io.datarouter.job.config.DatarouterJobSettingRoot;
import io.datarouter.job.lock.ClusterTriggerLockService;
import io.datarouter.job.lock.LocalTriggerLockService;
import io.datarouter.job.lock.TriggerLockConfig;
import io.datarouter.job.scheduler.JobWrapper.JobWrapperFactory;
import io.datarouter.tasktracker.scheduler.LongRunningTaskStatus;
import io.datarouter.tasktracker.service.LongRunningTaskService;
import io.datarouter.util.DateTool;
import io.datarouter.util.concurrent.ThreadTool;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.number.RandomTool;
import io.datarouter.web.exception.ExceptionRecorder;

/**
 * Provides scheduling, shouldRun, local and cluster locking for jobs.
 */
@Singleton
public class JobScheduler{
	private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

	private static final Duration LOCK_ACQUISITION_DELAY_INCREMENT = Duration.ofMillis(100);

	private final DatarouterInjector injector;
	private final DatarouterJobScheduler triggerExecutor;
	private final DatarouterJobExecutor jobExecutor;
	private final DatarouterJobSettingRoot jobSettings;
	private final LongRunningTaskService longRunningTaskService;
	private final JobCategoryTracker jobCategoryTracker;
	private final JobPackageTracker jobPackageTracker;
	private final LocalTriggerLockService localTriggerLockService;
	private final ClusterTriggerLockService clusterTriggerLockService;
	private final JobWrapperFactory jobWrapperFactory;
	private final JobCounters jobCounters;
	private final ExceptionRecorder exceptionRecorder;
	private final AtomicBoolean shutdown;

	@Inject
	public JobScheduler(
			DatarouterInjector injector,
			DatarouterJobScheduler triggerExecutor,
			DatarouterJobExecutor jobExecutor,
			DatarouterJobSettingRoot jobSettings,
			LongRunningTaskService longRunningTaskService,
			JobCategoryTracker jobCategoryTracker,
			JobPackageTracker jobPackageTracker,
			LocalTriggerLockService localTriggerLockService,
			ClusterTriggerLockService clusterTriggerLockService,
			JobWrapperFactory jobWrapperFactory,
			JobCounters jobCounters,
			ExceptionRecorder exceptionRecorder){
		this.injector = injector;
		this.triggerExecutor = triggerExecutor;
		this.jobExecutor = jobExecutor;
		this.jobSettings = jobSettings;
		this.longRunningTaskService = longRunningTaskService;
		this.jobCategoryTracker = jobCategoryTracker;
		this.jobPackageTracker = jobPackageTracker;
		this.localTriggerLockService = localTriggerLockService;
		this.clusterTriggerLockService = clusterTriggerLockService;
		this.jobWrapperFactory = jobWrapperFactory;
		this.jobCounters = jobCounters;
		this.exceptionRecorder = exceptionRecorder;
		this.shutdown = new AtomicBoolean();
	}

	public void registerTriggers(BaseTriggerGroup triggerGroup){
		triggerGroup.getJobPackages().forEach(this::register);
	}

	public boolean triggerManualJob(Class<? extends BaseJob> jobClass, String triggeredBy){
		JobPackage jobPackage = JobPackage.createManualFromScheduledPackage(jobPackageTracker.getForClass(jobClass));
		BaseJob job = injector.getInstance(jobClass);
		JobWrapper jobWrapper = jobWrapperFactory.createManual(jobPackage, job, triggeredBy);
		return triggerManual(jobWrapper);
	}

	public void shutdown(){
		shutdown.set(true); // stop scheduling new jobs
		localTriggerLockService.onShutdown(); // tell currently running jobs to stop
		triggerExecutor.shutdown(); // start rejecting new triggers
		ThreadTool.sleepUnchecked(5_000); // give some time for currently running jobs to stop
		jobExecutor.shutdownNow(); // interrupt all jobs
		triggerExecutor.shutdownNow(); // interrupt all triggers
		releaseThisServersActiveTriggerLocks();
		clusterTriggerLockService.releaseThisServersJobLocks();
	}

	/*-------------- schedule ----------------*/

	private void scheduleFirstRun(JobPackage jobPackage){
		Class<? extends BaseJob> jobClass = jobPackage.jobClass;
		if(!jobSettings.scheduleMissedJobsOnStartup.get()){
			scheduleNextRun(jobPackage);
			return;
		}
		if(!configuredToRun(jobPackage)){
			scheduleNextRun(jobPackage);
			return;
		}
		Optional<Date> lastCompletionTime = longRunningTaskService.findLastSuccessDate(jobClass.getSimpleName());
		if(lastCompletionTime.isEmpty()){//has never run, schedule for next normal time
			scheduleNextRun(jobPackage);
			return;
		}
		//getNextValidTimeAfter should be present, because only non-manual jobs get scheduled
		Date nextValidTime = jobPackage.getNextValidTimeAfter(lastCompletionTime.get()).get();
		boolean haventMissedNextTrigger = new Date().before(nextValidTime);
		if(haventMissedNextTrigger){
			scheduleNextRun(jobPackage);
			return;
		}
		scheduleMissedRunImmediately(jobPackage, nextValidTime);
	}

	private void scheduleNextRun(JobPackage jobPackage){
		long nowMs = System.currentTimeMillis();
		Optional<Long> optionalDelay = getDelayBeforeNextTriggerTimeMs(jobPackage, nowMs);
		if(optionalDelay.isEmpty()){
			logger.warn("couldn't schedule " + getClass() + " because no trigger defined");
			return;
		}
		Long delay = optionalDelay.get();
		BaseJob nextJobInstance = injector.getInstance(jobPackage.jobClass);
		Date nextTriggerTime = new Date(nowMs + delay);
		JobWrapper jobWrapper = jobWrapperFactory.createScheduled(jobPackage, nextJobInstance, nextTriggerTime,
				nextTriggerTime, getClass().getSimpleName());
		schedule(jobWrapper, delay, TimeUnit.MILLISECONDS);
	}

	private void scheduleMissedRunImmediately(JobPackage jobPackage, Date officialTriggerTime){
		logger.warn("scheduling {} with official triggerTime {} to run immediately", jobPackage.jobClass
				.getSimpleName(), DateTool.formatAlphanumeric(officialTriggerTime.getTime()));
		jobCounters.schedulingImmediately(jobPackage.jobClass);
		BaseJob nextJobInstance = injector.getInstance(jobPackage.jobClass);
		JobWrapper jobWrapper = jobWrapperFactory.createScheduled(jobPackage, nextJobInstance, officialTriggerTime,
				new Date(), getClass().getSimpleName() + " scheduleMissedRunImmediately");
		schedule(jobWrapper, 0, TimeUnit.MILLISECONDS);
	}

	private void schedule(JobWrapper jobWrapper, long delay, TimeUnit unit){
		if(shutdown.get()){
			logger.warn("Job scheduler is shutdown, not scheduling {}", jobWrapper.jobClass.getSimpleName());
			return;
		}
		try{
			triggerExecutor.schedule(() -> triggerScheduled(jobWrapper), delay, unit);
		}catch(RejectedExecutionException e){
			throw e;
		}
	}

	private Optional<Long> getDelayBeforeNextTriggerTimeMs(JobPackage jobPackage, long nowMs){
		return jobPackage.getNextValidTimeAfter(new Date())
				.map(Date::getTime)
				.map(time -> time - nowMs);
	}

	/*-------------- trigger/run ----------------*/

	private void triggerScheduled(JobWrapper jobWrapper){
		Class<? extends BaseJob> jobClass = jobWrapper.job.getClass();
		JobPackage jobPackage = jobWrapper.jobPackage;
		try{
			if(!configuredToRun(jobPackage)){
				return;
			}
			if(jobPackage.triggerLockConfig.isPresent()){
				Duration delay = delayLockAquisitionBasedOnCurrentWorkload();
				tryAcquireClusterLockAndRun(jobWrapper, jobPackage.triggerLockConfig.get(), delay);
			}else{
				tryAcquireLocalLockAndRun(jobWrapper);
			}
		}catch(Exception e){
			jobCounters.exception(jobClass);
			logger.warn("exception jobName={}", jobClass.getName(), e);
		}finally{
			try{
				scheduleNextRun(jobPackage);
			}catch(Exception e){
				logger.error("exception scheduling next run for {}", jobClass, e);
			}
		}
	}

	private boolean triggerManual(JobWrapper jobWrapper){
		JobPackage jobPackage = jobWrapper.jobPackage;
		return jobPackage.triggerLockConfig
				.map(triggerLockConfig -> tryAcquireClusterLockAndRun(jobWrapper, triggerLockConfig, Duration.ZERO))
				.orElseGet(() -> tryAcquireLocalLockAndRun(jobWrapper));
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

	private boolean tryAcquireClusterLockAndRun(
			JobWrapper jobWrapper,
			TriggerLockConfig triggerLockConfig,
			Duration delay){
		if(!clusterTriggerLockService.acquireJobAndTriggerLocks(triggerLockConfig, jobWrapper.triggerTime, delay)){
			return false;
		}
		try{
			boolean started = tryAcquireLocalLockAndRun(jobWrapper);
			if(!started){
				return false;
			}
		}finally{
			try{
				clusterTriggerLockService.releaseJobLock(triggerLockConfig, jobWrapper.triggerTime);
			}catch(Exception e){
				logger.warn("failed to release lockName {}", triggerLockConfig, e);
			}
		}
		return true;
	}

	private boolean tryAcquireLocalLockAndRun(JobWrapper jobWrapper){
		Class<? extends BaseJob> jobClass = jobWrapper.job.getClass();
		long hardTimeoutMs = getDeadlineMs(jobWrapper);
		if(!localTriggerLockService.acquire(jobWrapper)){
			return false;
		}
		Future<?> future;
		try{
			future = jobExecutor.submit(jobWrapper);
		}catch(RejectedExecutionException e){
			if(shutdown.get()){
				logger.warn("Job scheduler is shutdown, not running {}", jobWrapper.jobClass.getSimpleName());
				return false;
			}
			jobWrapper.setStatusFinishTimeAndPersist(LongRunningTaskStatus.ERRORED);
			var exception = new RuntimeException("rejected jobName=" + jobClass.getName(), e);
			exceptionRecorder.tryRecordException(exception, jobClass.getName(), JobExceptionCategory.JOB)
					.ifPresent(exceptionRecord -> jobWrapper.setExceptionRecordId(exceptionRecord.id));
			throw exception;
		}
		try{
			future.get(hardTimeoutMs, TimeUnit.MILLISECONDS);
			return true;
		}catch(InterruptedException e){
			future.cancel(true);
			jobWrapper.setStatusFinishTimeAndPersist(LongRunningTaskStatus.INTERRUPTED);
			var elapsedFromTrigger = new DatarouterDuration(System.currentTimeMillis() - jobWrapper.triggerTime
					.getTime(), TimeUnit.MILLISECONDS);
			var elapsedFromStart = jobWrapper.startedAt == null ? null : new DatarouterDuration(
					System.currentTimeMillis() - jobWrapper.startedAt.toEpochMilli(), TimeUnit.MILLISECONDS);
			var deadline = new DatarouterDuration(hardTimeoutMs, TimeUnit.MILLISECONDS);
			var exception = new RuntimeException("interrupted jobName=" + jobClass.getName() + " elapsedFromTrigger="
					+ elapsedFromTrigger + " elapsedFromStart=" + elapsedFromStart + " deadline=" + deadline, e);
			logger.warn("interrupted jobName={} elapsedFromTrigger={} elapsedFromStart={} deadline={}", jobClass
					.getName(), elapsedFromTrigger, elapsedFromStart, deadline);
			exceptionRecorder.tryRecordException(exception, jobClass.getName(), JobExceptionCategory.JOB)
					.ifPresent(exceptionRecord -> jobWrapper.setExceptionRecordId(exceptionRecord.id));
			jobCounters.interrupted(jobClass);
			return true;
		}catch(ExecutionException e){
			jobWrapper.setStatusFinishTimeAndPersist(LongRunningTaskStatus.ERRORED);
			var elapsed = new DatarouterDuration(System.currentTimeMillis() - jobWrapper.triggerTime.getTime(),
					TimeUnit.MILLISECONDS);
			var deadline = new DatarouterDuration(hardTimeoutMs, TimeUnit.MILLISECONDS);
			var exception = new RuntimeException("failed jobName=" + jobClass.getName() + " elapsed=" + elapsed
					+ " deadline=" + deadline, e);
			exceptionRecorder.tryRecordException(exception, jobClass.getName(), JobExceptionCategory.JOB)
					.ifPresent(exceptionRecord -> jobWrapper.setExceptionRecordId(exceptionRecord.id));
			throw exception;
		}catch(TimeoutException e){
			future.cancel(true);
			jobWrapper.setStatusFinishTimeAndPersist(LongRunningTaskStatus.TIMED_OUT);
			var elapsed = new DatarouterDuration(System.currentTimeMillis() - jobWrapper.triggerTime.getTime(),
					TimeUnit.MILLISECONDS);
			var deadline = new DatarouterDuration(hardTimeoutMs, TimeUnit.MILLISECONDS);
			var exception = new RuntimeException("didn't complete on time jobName=" + jobClass.getName() + " elapsed="
					+ elapsed + " deadline=" + deadline, e);
			exceptionRecorder.tryRecordException(exception, jobClass.getName(), JobExceptionCategory.JOB)
					.ifPresent(exceptionRecord -> jobWrapper.setExceptionRecordId(exceptionRecord.id));
			jobCounters.timedOut(jobClass);
			throw exception;
		}finally{
			localTriggerLockService.release(jobClass);
		}
	}

	private long getDeadlineMs(JobWrapper jobWrapper){
		return jobWrapper.jobPackage.getHardDeadline(jobWrapper.triggerTime)
				.map(deadline -> Duration.between(Instant.now(), deadline))
				.map(Duration::toMillis)
				.orElse(Long.MAX_VALUE);
	}

	/*---------------- helpers -------------------*/

	private void register(JobPackage jobPackage){
		jobPackageTracker.register(jobPackage);
		jobCategoryTracker.register(jobPackage.jobCategoryName);
		scheduleFirstRun(jobPackage);
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
			Date triggerTime = jobWrapper.triggerTime;
			clusterTriggerLockService.releaseTriggerLock(jobName, triggerTime);
		});
	}

}
