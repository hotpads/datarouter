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
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.trace.Tracer;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.job.BaseJob;
import io.datarouter.job.JobCounters;
import io.datarouter.tasktracker.scheduler.LongRunningTaskStatus;
import io.datarouter.tasktracker.scheduler.LongRunningTaskType;
import io.datarouter.tasktracker.service.LongRunningTaskTracker;
import io.datarouter.tasktracker.service.LongRunningTaskTrackerFactory;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.duration.DatarouterDuration;

/**
 * A wrapper around jobs for instrumentation purposes.
 */
public class JobWrapper implements Callable<Void>{
	private static final Logger logger = LoggerFactory.getLogger(JobWrapper.class);

	@Singleton
	public static class JobWrapperFactory{

		@Inject
		private LongRunningTaskTrackerFactory longRunningTaskTrackerFactory;
		@Inject
		private JobCounters jobCounters;

		public JobWrapper createScheduled(
				JobPackage jobPackage,
				BaseJob job,
				Instant triggerTime,
				Instant scheduledTime,
				String triggeredBy){
			return new JobWrapper(
					jobPackage,
					longRunningTaskTrackerFactory,
					jobCounters,
					job,
					triggerTime,
					scheduledTime,
					true,
					triggeredBy);
		}

		public JobWrapper createRetriggered(
				JobPackage jobPackage,
				BaseJob job,
				Instant triggerTime,
				Instant scheduledTime,
				String triggeredBy){
			return new JobWrapper(
					jobPackage,
					longRunningTaskTrackerFactory,
					jobCounters,
					job,
					triggerTime,
					scheduledTime,
					false,
					triggeredBy);
		}

		public JobWrapper createManual(JobPackage jobPackage, BaseJob job, String triggeredBy){
			Instant now = Instant.now();
			return new JobWrapper(
					jobPackage,
					longRunningTaskTrackerFactory,
					jobCounters,
					job,
					now,
					now,
					false,
					triggeredBy);
		}

		public JobWrapper createRequestTriggered(BaseJob job, String triggeredBy){
			Instant now = Instant.now();
			return new JobWrapper(longRunningTaskTrackerFactory, jobCounters, job, now, now, false, triggeredBy);
		}
	}

	//singletons
	private final JobCounters jobCounters;
	//final fields
	public final JobPackage jobPackage;
	public final BaseJob job;
	public final Instant triggerTime;//time the job should run, used for locking
	public final Instant scheduledTime;//can be different from triggerTime if a job is scheduled late
	public final boolean reschedule;//not created by the normal scheduler
	public final String triggeredBy;
	protected final LongRunningTaskTracker tracker;
	//convenience
	public final Class<? extends BaseJob> jobClass;

	private JobWrapper(
			JobPackage jobPackage,
			LongRunningTaskTrackerFactory longRunningTaskTrackerFactory,
			JobCounters jobCounters,
			BaseJob job,
			Instant triggerTime,
			Instant scheduledTime,
			boolean reschedule,
			String triggeredBy){
		this(
				jobPackage,
				jobCounters,
				job,
				triggerTime,
				scheduledTime,
				reschedule,
				triggeredBy,
				initTracker(jobPackage, job.getClass(), triggerTime, scheduledTime, longRunningTaskTrackerFactory,
						triggeredBy));
	}

	protected JobWrapper(
			LongRunningTaskTrackerFactory longRunningTaskTrackerFactory,
			JobCounters jobCounters,
			BaseJob job,
			Instant triggerTime,
			Instant scheduledTime,
			boolean reschedule,
			String triggeredBy){
		this(
				null,
				jobCounters,
				job,
				triggerTime,
				scheduledTime,
				reschedule,
				triggeredBy,
				initTracker(null, job.getClass(), triggerTime, scheduledTime, longRunningTaskTrackerFactory,
						triggeredBy));
	}

	protected JobWrapper(
			JobPackage jobPackage,
			JobCounters jobCounters,
			BaseJob job,
			Instant triggerTime,
			Instant scheduledTime,
			boolean reschedule,
			String triggeredBy,
			LongRunningTaskTracker taskTracker){
		this.jobPackage = jobPackage;
		this.jobCounters = jobCounters;
		this.job = job;
		this.triggerTime = triggerTime;
		this.scheduledTime = scheduledTime;
		this.reschedule = reschedule;
		this.triggeredBy = triggeredBy;
		this.jobClass = job.getClass();
		this.tracker = taskTracker;
	}

	@Override
	public Void call() throws Exception{
		startTraceSummary();
		trackBefore();
		job.run(tracker);
		trackAfter();
		endTraceSummary();
		logSuccess();
		return null;
	}

	public void requestStop(){
		tracker.requestStop();
	}

	public void finishWithStatus(LongRunningTaskStatus status){
		tracker.onFinish(status.getStatus());
	}

	public void setExceptionRecordId(String exceptionRecordId){
		tracker.setExceptionRecordId(exceptionRecordId);
	}

	private static LongRunningTaskTracker initTracker(
			JobPackage jobPackage,
			Class<? extends BaseJob> jobClass,
			Instant triggerTime,
			Instant scheduledTime,
			LongRunningTaskTrackerFactory longRunningTaskTrackerFactory,
			String triggeredBy){
		Instant deadline = Optional.ofNullable(jobPackage)
				.flatMap(jp -> jp.getSoftDeadline(triggerTime))
				.orElse(null);
		boolean warnOnReachingDeadline = Optional.ofNullable(jobPackage)
				.flatMap(JobPackage::getWarnOnReachingDuration)
				.orElse(false);
		return longRunningTaskTrackerFactory.create(
				jobClass,
				LongRunningTaskType.JOB,
				deadline,
				warnOnReachingDeadline,
				triggeredBy)
				.setTriggerTime(triggerTime)
				.setScheduledTime(scheduledTime);
	}

	protected void startTraceSummary(){
		if(logger.isInfoEnabled()){
//			TracerThreadLocal.bindToThread(new DatarouterSummaryTracer());
		}
	}

	protected void endTraceSummary(){
		Tracer tracer = TracerThreadLocal.get();
		if(tracer != null){
			logger.info("job={}, {}", jobClass.getSimpleName(), TracerThreadLocal.get());
			TracerThreadLocal.clearFromThread();
		}
	}

	protected void trackBefore(){
		jobCounters.started(jobClass);
		tracker.onStart();
	}

	protected void trackAfter(){
		jobCounters.finished(jobClass);
		tracker.onFinish();
	}

	@SuppressWarnings("unused")
	private void tryPersistTracker(){
		try{
			tracker.doReportTasks();
		}catch(Exception e){
			logger.error("error persisting LongRunningTask after job: {}", tracker.getName(), e);
		}
	}

	protected void logSuccess(){
		long startDelayMs = Duration.between(scheduledTime, tracker.getStartTime()).toMillis();
		Duration elapsedTime = Duration.between(tracker.getStartTime(), tracker.getFinishTime());
		jobCounters.duration(jobClass, elapsedTime);
		Optional<Instant> nextJobTriggerTime = jobPackage.getNextValidTimeAfter(Date.from(scheduledTime))
				.map(Date::toInstant);
		String jobCompletionLog = "finished in " + new DatarouterDuration(elapsedTime) + " jobName="
				+ jobClass.getSimpleName() + " durationMs=" + elapsedTime.toMillis();
		if(startDelayMs > 1000){
			jobCounters.startedAfterLongDelay(jobClass);
			jobCompletionLog += " startDelayMs= " + startDelayMs;
		}
		if(nextJobTriggerTime.isPresent() && Instant.now().isAfter(nextJobTriggerTime.get())){
			jobCounters.missedNextTrigger(jobClass);
			jobCompletionLog += " missed next trigger";
		}
		if(ComparableTool.gt(elapsedTime, Duration.ofHours(3))){
			logger.warn("long-running job " + jobCompletionLog);
		}else if(ComparableTool.gt(elapsedTime, Duration.ofMillis(500))){
			logger.warn(jobCompletionLog);
		}else{
			logger.info(jobCompletionLog);
		}
	}

}
