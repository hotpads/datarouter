package com.hotpads.job.trigger;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.quartz.CronExpression;

import com.google.inject.Injector;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.job.record.JobExecutionStatus;
import com.hotpads.job.record.LongRunningTask;
import com.hotpads.job.record.LongRunningTaskKey;
import com.hotpads.job.thread.JobExecutorProvider.JobExecutor;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.ObjectTool;

@Singleton
public class JobScheduler {
	private static Logger logger = Logger.getLogger(JobScheduler.class);

	private Injector injector;
	private ScheduledExecutorService executor;
	private TriggerGroup triggerGroup;
	private TriggerTracker tracker;
	private IndexedSortedMapStorageNode<LongRunningTaskKey,LongRunningTask> longRunningTaskNode;
	
	@Inject
	public JobScheduler(Injector injector, TriggerGroup triggerGroup, TriggerTracker tracker, 
			IndexedSortedMapStorageNode<LongRunningTaskKey, LongRunningTask> node, @JobExecutor ScheduledExecutorService executor){
		this.injector = injector;
		this.triggerGroup = triggerGroup;
		this.tracker = tracker;
		this.executor = executor;
		this.longRunningTaskNode = node;
	}
	
	
	/***************methods***************/
	
	public void scheduleJavaTriggers(){
		Map<String, Date> jobsLastCompletion = MapTool.create();
		for(Map.Entry<Class<? extends Job>, String> entry : triggerGroup.getJobClasses().entrySet()){
			jobsLastCompletion.put(entry.getKey().getSimpleName(), null);
		}
		String currentJobClass = "";
		for(LongRunningTask task : longRunningTaskNode.scan(null, null)){
			if(!currentJobClass.equals(task.getKey().getJobClass())){
				currentJobClass = task.getKey().getJobClass();
			}
			if(task.getJobExecutionStatus() == JobExecutionStatus.success){
				jobsLastCompletion.put(task.getKey().getJobClass(), task.getFinishTime());
			}
		}
		for(Entry<Class<? extends Job>, String> entry : triggerGroup.getJobClasses().entrySet()){
			tracker.createNewTriggerInfo(entry.getKey());
			Job sampleJob = injector.getInstance(entry.getKey());
			if(!sampleJob.shouldRun()){
				sampleJob.scheduleNextRun(false);
				logger.warn("scheduled " + sampleJob.getClass().getSimpleName() + " to run with delay");
			}else{
				try {
					CronExpression cron = new CronExpression(entry.getValue());
					if(jobsLastCompletion.get(entry.getKey().getSimpleName()) == null){
						sampleJob.scheduleNextRun(true);
						logger.warn("scheduled " + sampleJob.getClass().getSimpleName() + " to run immediately");
						continue;
					}
					Date nextValid = cron.getNextValidTimeAfter(jobsLastCompletion.get(entry.getKey().getSimpleName()));
					if(new Date().after(nextValid)){
						sampleJob.scheduleNextRun(true);
						logger.warn("scheduled " + sampleJob.getClass().getSimpleName() + " to run immediately");
					}else{
						sampleJob.scheduleNextRun(false);
						logger.warn("scheduled " + sampleJob.getClass().getSimpleName() + " to run with delay");
					}
				} catch (ParseException e) {
					ExceptionTool.getStackTraceAsString(e);
					sampleJob.scheduleNextRun(false);
					logger.warn("scheduled " + sampleJob.getClass().getSimpleName() + " to run with delay");
				}
	//			logger.warn("scheduled "+jobClass+" at "+sampleJob.getTrigger().getCronExpression());
			}
		}
	}
	
	public Job getJobInstance(Class<? extends Job> jobClass, String cronExpression){
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
