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
import com.hotpads.setting.Setting;
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
	private Setting<Boolean> scheduleMissedJobsOnStartup;
	
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
		Map<String, Date> jobsLastCompletion = loadJobsLastCompletionFromLongRunningTasks();
		for(Entry<Class<? extends Job>, String> entry : triggerGroup.getJobClasses().entrySet()){
			tracker.createNewTriggerInfo(entry.getKey());
			Job sampleJob = injector.getInstance(entry.getKey());
			if(!scheduleMissedJobsOnStartup.getValue() || !sampleJob.shouldRun()){
				sampleJob.scheduleNextRun(false);
			}else{
				try {
					CronExpression cron = new CronExpression(entry.getValue());
					if(!jobsLastCompletion.containsKey(entry.getKey().getSimpleName())){
						sampleJob.scheduleNextRun(true);
						continue;
					}
					Date nextValid = cron.getNextValidTimeAfter(jobsLastCompletion.get(entry.getKey().getSimpleName()));
					if(new Date().after(nextValid)){
						sampleJob.scheduleNextRun(true);
					}else{
						sampleJob.scheduleNextRun(false);
					}
				} catch (ParseException e) {
					logger.error(ExceptionTool.getStackTraceAsString(e));
					sampleJob.scheduleNextRun(false);
				}
	//			logger.warn("scheduled "+jobClass+" at "+sampleJob.getTrigger().getCronExpression());
			}
		}
	}
	
	private Map<String, Date> loadJobsLastCompletionFromLongRunningTasks(){
		Map<String, Date> jobsLastCompletion = MapTool.create();
		for(LongRunningTask task : longRunningTaskNode.scan(null, null)){
			if(task.getJobExecutionStatus() == JobExecutionStatus.success){
				jobsLastCompletion.put(task.getKey().getJobClass(), task.getFinishTime());
			}
		}
		return jobsLastCompletion;
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
	
	public Setting<Boolean> getScheduleMissedJobsOnStartupSetting() {
		return scheduleMissedJobsOnStartup;
	}

	public void setScheduleMissedJobsOnStartupSetting(Setting<Boolean> scheduleMissedJobsOnStartup) {
		this.scheduleMissedJobsOnStartup = scheduleMissedJobsOnStartup;
	}
}
