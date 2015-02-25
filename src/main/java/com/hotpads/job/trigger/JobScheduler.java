package com.hotpads.job.trigger;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.DatarouterInjector;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.guice.DatarouterExecutorGuiceModule;
import com.hotpads.job.record.JobExecutionStatus;
import com.hotpads.job.record.LongRunningTask;
import com.hotpads.job.record.LongRunningTaskDao;
import com.hotpads.job.record.LongRunningTaskKey;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.ObjectTool;

@Singleton
public class JobScheduler {
	private static Logger logger = LoggerFactory.getLogger(JobScheduler.class);

	private DatarouterInjector injector;
	private ScheduledExecutorService executor;
	private TriggerGroup triggerGroup;
	private TriggerTracker tracker;
	private IndexedSortedMapStorage<LongRunningTaskKey,LongRunningTask> longRunningTaskNode;
	private JobSettings jobSettings;
	
	@Inject
	public JobScheduler(DatarouterInjector injector, TriggerGroup triggerGroup, TriggerTracker tracker,
			LongRunningTaskDao longRunningTaskDao, JobSettings jobSettings,
			@Named(DatarouterExecutorGuiceModule.POOL_datarouterJobExecutor) ScheduledExecutorService executor){
		this.injector = injector;
		this.triggerGroup = triggerGroup;
		this.tracker = tracker;
		this.jobSettings = jobSettings;
		this.executor = executor;
		this.longRunningTaskNode = longRunningTaskDao.getNode();
	}
	
	/***************methods***************/
	
	public void scheduleJavaTriggers(){
		Map<String, Date> jobsLastCompletion = loadJobsLastCompletionFromLongRunningTasks();
		for(Entry<Class<? extends Job>, String> entry : triggerGroup.getJobClasses().entrySet()){
			tracker.createNewTriggerInfo(entry.getKey());
			Job sampleJob = injector.getInstance(entry.getKey());
			if(!jobSettings.getScheduleMissedJobsOnStartup().getValue() || !sampleJob.shouldRun()){
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
					logger.error("", e);
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
