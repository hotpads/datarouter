package com.hotpads.job.trigger;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.hotpads.datarouter.inject.DatarouterInjector;
import com.hotpads.datarouter.inject.guice.executor.DatarouterExecutorGuiceModule;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.job.record.JobExecutionStatus;
import com.hotpads.job.record.LongRunningTask;
import com.hotpads.job.record.LongRunningTaskKey;
import com.hotpads.job.record.LongRunningTaskNodeProvider;
import com.hotpads.job.web.TriggersRepository.JobPackage;
import com.hotpads.util.core.date.CronExpression;

@Singleton
public class JobScheduler {
	private final DatarouterInjector injector;
	private final ScheduledExecutorService executor;
	private final TriggerTracker tracker;
	private final IndexedSortedMapStorage<LongRunningTaskKey,LongRunningTask> longRunningTaskNode;
	private final JobSettings jobSettings;

	@Inject
	public JobScheduler(DatarouterInjector injector, TriggerTracker tracker,
			LongRunningTaskNodeProvider longRunningTaskNodeProvider, JobSettings jobSettings,
			@Named(DatarouterExecutorGuiceModule.POOL_datarouterJobExecutor) ScheduledExecutorService executor){
		this.injector = injector;
		this.tracker = tracker;
		this.jobSettings = jobSettings;
		this.executor = executor;
		this.longRunningTaskNode = longRunningTaskNodeProvider.get();
	}

	/***************methods***************/

	public void scheduleJobPackage(JobPackage jobPackage){
		tracker.createNewTriggerInfo(jobPackage.jobClass);
		Job sampleJob = injector.getInstance(jobPackage.jobClass);
		if(!jobSettings.getScheduleMissedJobsOnStartup().getValue() || !sampleJob.shouldRun()){
			sampleJob.scheduleNextRun(false);
			return;
		}
		Optional<Date> jobsLastCompletion = longRunningTaskNode.streamWithPrefix(new LongRunningTaskKey(
				jobPackage.jobClass), null)
				.filter(task -> task.getJobExecutionStatus() == JobExecutionStatus.success)
				.map(LongRunningTask::getFinishTime)
				.max(Date::compareTo);
		if(!jobsLastCompletion.isPresent()){
			sampleJob.scheduleNextRun(true);
			return;
		}
		Date nextValid = jobPackage.cronExpression.getNextValidTimeAfter(jobsLastCompletion.get());
		sampleJob.scheduleNextRun(new Date().after(nextValid));
	}

	public Job getJobInstance(Class<? extends Job> jobClass){
		Job sampleJob = injector.getInstance(jobClass);
		if(tracker.get(jobClass).getLastFired() != null){
			long lastIntervalDurationMs = System.currentTimeMillis() - tracker.get(jobClass).getLastFired().getTime();
			tracker.get(jobClass).setLastIntervalDurationMs(lastIntervalDurationMs);
		}else{
			tracker.get(jobClass).setLastIntervalDurationMs(0);
		}
		CronExpression trigger = sampleJob.getTrigger();
		if(trigger != null){
			tracker.get(jobClass).setLastFired(new Date());
		}
		String defaultCronExpression = sampleJob.getDefaultTrigger().getCronExpression();
		String thisCronExpression = sampleJob.getTrigger().getCronExpression();
		boolean isCustom = DrObjectTool.notEquals(defaultCronExpression, thisCronExpression);
		tracker.get(jobClass).setCustom(isCustom);
		return sampleJob;
	}

	public void shutDownNow(){
		executor.shutdownNow();
	}
	/***************getters/setters***************/

	public TriggerTracker getTracker(){
		return tracker;
	}

}
