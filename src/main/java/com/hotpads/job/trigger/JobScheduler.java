package com.hotpads.job.trigger;

import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.quartz.CronExpression;

import com.google.inject.Injector;
import com.hotpads.job.thread.JobExecutorProvider.JobExecutor;
import com.hotpads.util.core.ObjectTool;

@Singleton
public class JobScheduler {
	protected static Logger logger = Logger.getLogger(JobScheduler.class);

	private Injector injector;
	private ScheduledExecutorService executor;
	private TriggerGroup triggerGroup;
	private TriggerTracker tracker;
	
	@Inject
	public JobScheduler(Injector injector, TriggerGroup triggerGroup, TriggerTracker tracker, 
			@JobExecutor ScheduledExecutorService executor){
		this.injector = injector;
		this.triggerGroup = triggerGroup;
		this.tracker = tracker;
		this.executor = executor;
		
	}
	
	
	/***************methods***************/
	
	public void scheduleJavaTriggers(){
		for(Entry<Class<? extends BaseJob>, String> entry : triggerGroup.getJobClasses().entrySet()){
			tracker.createNewTriggerInfo(entry.getKey());
			Job sampleJob = injector.getInstance(entry.getKey());
			sampleJob.scheduleNextRun();
//			logger.warn("scheduled "+jobClass+" at "+sampleJob.getTrigger().getCronExpression());
		}
	}
	
	public BaseJob getJobInstance(Class<? extends BaseJob> jobClass, String cronExpression){
		BaseJob sampleJob = injector.getInstance(jobClass);
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
		boolean isCustom = ObjectTool.notEquals(defaultCronExpression, thisCronExpression);
		tracker.get(jobClass).setCustom(isCustom);
//		logger.warn("created "+jobClass.getSimpleName()+" "+System.identityHashCode(sampleJob));
		return sampleJob;
	}
	
	public void shutDownNow(){
		executor.shutdownNow();
	}
	/***************getters/setters***************/
	
	public TriggerTracker getTracker(){
		return tracker;
	}
	
	public TriggerGroup getTriggerGroup(){
		return triggerGroup;
	}
	
}
