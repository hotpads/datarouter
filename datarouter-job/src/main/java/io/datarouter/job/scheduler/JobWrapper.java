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
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskStatus;
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
import io.datarouter.util.tracer.DatarouterSummaryTracer;

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
				Date triggerTime,
				Date scheduledTime,
				String triggeredBy){
			return new JobWrapper(jobPackage, longRunningTaskTrackerFactory, jobCounters, job, triggerTime,
					scheduledTime, triggeredBy);
		}

		public JobWrapper createManual(JobPackage jobPackage, BaseJob job, String triggeredBy){
			Date now = new Date();
			return new JobWrapper(jobPackage, longRunningTaskTrackerFactory, jobCounters, job, now, now, triggeredBy);
		}

		public JobWrapper createRequestTriggered(BaseJob job, String triggeredBy){
			Date now = new Date();
			return new JobWrapper(longRunningTaskTrackerFactory, jobCounters, job, now, now, triggeredBy);
		}
	}

	//singletons
	private final JobCounters jobCounters;
	//final fields
	public final JobPackage jobPackage;
	public final BaseJob job;
	public final Date triggerTime;//time the job should run, used for locking
	public final Date scheduledTime;//can be different from triggerTime if a job is scheduled late
	public final String triggeredBy;
	private final LongRunningTaskTracker tracker;
	//convenience
	public final Class<? extends BaseJob> jobClass;
	//mutable tracking fields
	private Instant startedAt;

	private JobWrapper(
			JobPackage jobPackage,
			LongRunningTaskTrackerFactory longRunningTaskTrackerFactory,
			JobCounters jobCounters,
			BaseJob job,
			Date triggerTime,
			Date scheduledTime,
			String triggeredBy){
		this.jobPackage = jobPackage;
		this.jobCounters = jobCounters;
		this.job = job;
		this.triggerTime = triggerTime;
		this.scheduledTime = scheduledTime;
		this.triggeredBy = triggeredBy;
		this.jobClass = job.getClass();
		this.tracker = initTracker(jobPackage, scheduledTime, longRunningTaskTrackerFactory, triggeredBy, jobClass);
	}

	private JobWrapper(
			LongRunningTaskTrackerFactory longRunningTaskTrackerFactory,
			JobCounters jobCounters,
			BaseJob job,
			Date triggerTime,
			Date scheduledTime,
			String triggeredBy){
		this.jobPackage = null;
		this.jobCounters = jobCounters;
		this.job = job;
		this.triggerTime = triggerTime;
		this.scheduledTime = scheduledTime;
		this.triggeredBy = triggeredBy;
		this.jobClass = job.getClass();
		this.tracker = longRunningTaskTrackerFactory.create(jobClass, LongRunningTaskType.JOB, null, false,
				triggeredBy);
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

	public void setStatusFinishTimeAndPersist(LongRunningTaskStatus status){
		tracker.onFinish();
		tracker.setStatus(status.getStatus());
		tryPersistTracker();
	}

	public void setExceptionRecordId(String exceptionRecordId){
		tracker.setExceptionRecordId(exceptionRecordId);
	}

	private static LongRunningTaskTracker initTracker(
			JobPackage jobPackage,
			Date triggerTime,
			LongRunningTaskTrackerFactory longRunningTaskTrackerFactory,
			String triggeredBy,
			Class<? extends BaseJob> jobClass){
		Instant deadline = jobPackage.getSoftDeadline(triggerTime).orElse(null);
		boolean warnOnReachingDeadline = jobPackage.getWarnOnReachingDuration().orElse(false);
		return longRunningTaskTrackerFactory.create(jobClass, LongRunningTaskType.JOB, deadline, warnOnReachingDeadline,
				triggeredBy);
	}

	private void startTraceSummary(){
		if(logger.isInfoEnabled()){
			TracerThreadLocal.bindToThread(new DatarouterSummaryTracer());
		}
	}

	private void endTraceSummary(){
		Tracer tracer = TracerThreadLocal.get();
		if(tracer != null){
			logger.info("job={}, {}", jobClass.getSimpleName(), TracerThreadLocal.get());
			TracerThreadLocal.clearFromThread();
		}
	}

	private void trackBefore(){
		jobCounters.started(jobClass);
		startedAt = Instant.now();
		tracker.setStartTime(startedAt);
		tracker.setStatus(TaskStatus.RUNNING);
		tracker.setScheduledTime(scheduledTime.toInstant());
		tracker.persistIfShould();
	}

	private void trackAfter(){
		jobCounters.finished(jobClass);
		tracker.onFinish();
		if(tracker.getStatus() == TaskStatus.RUNNING){
			tracker.setStatus(TaskStatus.SUCCESS);
		}
		tryPersistTracker();
	}

	private void tryPersistTracker(){
		try{
			tracker.persistIfShould();
		}catch(Exception e){
			logger.error("error persisting LongRunningTask after job: {}", tracker.getName(), e);
		}
	}

	private void logSuccess(){
		long startDelayMs = startedAt.toEpochMilli() - scheduledTime.getTime();
		Duration elapsedTime = Duration.between(startedAt, tracker.getFinishTime());
		jobCounters.duration(jobClass, elapsedTime);
		Optional<Date> nextJobTriggerTime = jobPackage.getNextValidTimeAfter(scheduledTime);
		String jobCompletionLog = "finished in " + new DatarouterDuration(elapsedTime) + " jobName="
				+ jobClass.getSimpleName() + " durationMs=" + elapsedTime.toMillis();
		if(startDelayMs > 1000){
			jobCounters.startedAfterLongDelay(jobClass);
			jobCompletionLog += " startDelayMs= " + startDelayMs;
		}
		if(nextJobTriggerTime.isPresent() && new Date().after(nextJobTriggerTime.get())){
			jobCounters.missedNextTrigger(jobClass);
			jobCompletionLog += " missed next trigger";
		}
		if(ComparableTool.gt(elapsedTime, Duration.ofMillis(500))){
			logger.warn(jobCompletionLog);
		}else{
			logger.info(jobCompletionLog);
		}
	}

}
