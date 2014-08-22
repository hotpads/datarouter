package com.hotpads.job.trigger;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.handler.exception.ExceptionHandlingConfig;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.handler.exception.ExceptionRecordKey;
import com.hotpads.job.record.JobExecutionStatus;
import com.hotpads.job.record.LongRunningTaskTracker;
import com.hotpads.job.record.LongRunningTaskType;
import com.hotpads.notification.ParallelApiCaller;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.databean.NotificationUserType;
import com.hotpads.setting.Setting;
import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.datastructs.MutableBoolean;

public abstract class BaseJob implements Job{
	private static Logger baseJobLogger = LoggerFactory.getLogger(BaseJob.class);
	protected Logger logger = LoggerFactory.getLogger(getClass());//for subclasses to use

	protected JobScheduler scheduler;
	protected ScheduledExecutorService executor;
	protected Setting<Boolean> processJobsSetting;
	protected boolean isAlreadyScheduled;
	protected boolean isAlreadyRunning;
	protected MutableBoolean interrupted = new MutableBoolean(false);
	protected LongRunningTaskTracker tracker;
	protected Setting<Boolean> shouldSaveLongRunningTasks;
	private String serverName;
	private Date triggerTime;
	private String jobClass;
	
	@Inject
	private SortedMapStorageNode<ExceptionRecordKey, ExceptionRecord> exceptionRecordNode;
	@Inject
	private ParallelApiCaller apiCaller;
	@Inject
	private ExceptionHandlingConfig exceptionHandlingConfig;

	/************************* constructors *******************/

	@Inject
	public BaseJob(JobEnvironment jobEnvironment) {
		this.scheduler = jobEnvironment.getScheduler();
		this.executor = jobEnvironment.getExecutor();
		this.processJobsSetting = jobEnvironment.getProcessJobsSetting();
		this.shouldSaveLongRunningTasks = jobEnvironment.getShouldSaveLongRunningTasksSetting();
		this.jobClass = getClass().getSimpleName();
		this.serverName = jobEnvironment.getServerName();
		this.tracker = jobEnvironment.getLongRunningTaskTrackerFactory().create(jobClass, serverName, 
				jobEnvironment.getShouldSaveLongRunningTasksSetting(), LongRunningTaskType.job);
	}

	/*********************** methods ******************************/

	public String getJobClass(){
		return jobClass;
	}
	
	@Override
	public Long getDelayBeforeNextFireTimeMs(){
		CronExpression trigger = getTrigger();
		if(trigger == null){ return null; }
		Date nextFireDate = trigger.getNextValidTimeAfter(new Date());
		if(nextFireDate==null){ return null; }
		long delay = nextFireDate.getTime() - System.currentTimeMillis();
		return delay;
	}

	@Override
	public void scheduleNextRun(boolean immediate){
		Long delay;
		if(immediate){
			delay = 0L;
			logger.warn("scheduling " + this.getClass().getSimpleName() + " to run immediately");
		}else{
			delay = getDelayBeforeNextFireTimeMs();
			if(delay==null){ 
				baseJobLogger.warn("couldn't schedule "+getClass()+" because no trigger defined");
				return;
			}
			if(isAlreadyScheduled){
				baseJobLogger.warn("couldn't schedule "+getClass()+" because is already scheduled");
				return;
			}
		}
		Job nextJobInstance = scheduler.getJobInstance(getClass(), getTrigger().getCronExpression());
		Long nextTriggerTime = System.currentTimeMillis() + delay;
		nextJobInstance.setTriggerTime(new Date(nextTriggerTime));
//		nextJobInstance.getLongRunningTaskTracker().getTask().setTriggerTime(new Date(nextTriggerTime));
//		if(shouldSaveLongRunningTasks.getValue()){
//			nextJobInstance.getLongRunningTaskTracker().getNode().put(nextJobInstance.getLongRunningTaskTracker().getTask(), null);
//		}
		executor.schedule(nextJobInstance, delay, TimeUnit.MILLISECONDS);
		isAlreadyScheduled = true;
//		logger.warn("scheduled next execution of "+getClass()+" for "
//				+DateTool.getYYYYMMDDHHMMSSMMMWithPunctuationNoSpaces(new Date(System.currentTimeMillis()+delay)));
	}

	@Override
	public Void call(){
		assertBaseServicesSet();
		try{
			runInternal();
		}catch(RuntimeException e){
			getFromTracker().incrementNumberOfErrors();
			getFromTracker().setLastErrorTime(new Date());
			baseJobLogger.warn("exception executing "+getClass(), e);
			recordException(e);
		}finally{
			try{
				if(!isAlreadyRunning){
					getFromTracker().setRunning(false);
				}else{
					baseJobLogger.warn("couldn't run "+getClass()+" because it is already running");
				}
			}catch(Exception e){
				baseJobLogger.warn("exception in finally block", e);
			}
			try{
				isAlreadyScheduled = false;
				scheduleNextRun(false);
			}catch(Exception e){
				baseJobLogger.warn("exception in finally block", e);
			}
		}
		return null;
	}

	public void recordException(Exception e) {
		ExceptionRecord exceptionRecord = new ExceptionRecord(
				serverName,
				ExceptionTool.getStackTraceAsString(e),
				e.getClass().getName());
		exceptionRecordNode.put(exceptionRecord, null);
		NotificationRequest notificationRequest = new NotificationRequest(
				new NotificationUserId(
						NotificationUserType.EMAIL,
						exceptionHandlingConfig.getRecipientEmail()),
				exceptionHandlingConfig.getJobErrorNotificationType(),
				exceptionRecord.getKey().getId(),
				getClass().getName());
		apiCaller.add(notificationRequest, exceptionRecord);
	}

	@Override
	public void trackBeforeRun(Long startTime){
		tracker.getTask().setStartTime(new Date(startTime));
		tracker.getTask().setJobExecutionStatus(JobExecutionStatus.running);
		tracker.getTask().setTriggerTime(triggerTime);
		if(shouldSaveLongRunningTasks.getValue()){
			tracker.getNode().put(tracker.getTask(), null);
		}
	}
	
	@Override
	public void runInternal(){
		if(shouldRunInternal()){
			scheduler.getTracker().get(this.getClass()).setRunning(true);
			scheduler.getTracker().get(this.getClass()).setJob(this);
			long startTimeMs = System.currentTimeMillis();
			trackBeforeRun(startTimeMs);
			run();
			long endTimeMs = System.currentTimeMillis();
			trackAfterRun(endTimeMs);
			long durationMs = endTimeMs - startTimeMs;
			scheduler.getTracker().get(this.getClass()).setLastExecutionDurationMs(durationMs);
			scheduler.getTracker().get(this.getClass()).incrementNumberOfSuccesses();
			Date nextJobTriggerTime = getTrigger().getNextValidTimeAfter(triggerTime);
			String jobCompletionLog = "Finished "+getClass().getSimpleName()+" in "+durationMs+"ms";
			if(new Date().after(nextJobTriggerTime)){
				jobCompletionLog = jobCompletionLog + ", missed next trigger";
			}
			baseJobLogger.warn(jobCompletionLog);
		}else{
//			baseJobLogger.warn(getClass()+" shouldRun=false");
		}
	}
	
	@Override
	public void trackAfterRun(Long endTime){
		if(tracker.getTask().getJobExecutionStatus() != JobExecutionStatus.interrupted){
			tracker.getTask().setFinishTime(new Date(endTime));
			tracker.getTask().setJobExecutionStatus(JobExecutionStatus.success);
		}
		if(shouldSaveLongRunningTasks.getValue()){
			tracker.getNode().put(tracker.getTask(), null);
		}
	}
	
	protected boolean shouldRunInternal(){
		if(getFromTracker().isRunning()){
			isAlreadyRunning = true;
			return false;
		}
		return processJobsSetting.getValue() && shouldRun() && BooleanTool.isFalse(getIsDisabled());
	}

	protected void assertBaseServicesSet(){
		if(scheduler==null || executor==null || processJobsSetting==null){
			baseJobLogger.error("you must call job.setScheduler(..), job.setExecutor(..), and job.setSettings(..) if"
					+" manually instantiating this job.  It is recommended to get an instance from the Injector instead.");
		}
		if(scheduler==null){
			throw new NullPointerException("please call job.setScheduler(JobScheduler scheduler)");
		}
		if(executor==null){
			throw new NullPointerException("please call job.setExecutor(ScheduledExecutorService executor)");
		}
		if(processJobsSetting==null){
			throw new NullPointerException("please call job.setProcessJobsSetting(..)");
		}
	}

	@Override
	public Date getNextScheduled() {
		CronExpression trigger = getTrigger();
		if(trigger == null){ return null; }
		Date nextFireDate = trigger.getNextValidTimeAfter(new Date());
		if(nextFireDate==null){ return null; }
		getFromTracker().setNextScheduled(nextFireDate);
		return getFromTracker().getNextScheduled();
	}

	@Override
	public Date getLastFired(){
		return getFromTracker().getLastFired();
	}
	
	@Override
	public String getLastErrorTime() {
		if(getFromTracker().getLastErrorTime() == null){
			return "No error detected";
		}
		return getFromTracker().getLastErrorTime().toString();
	}

	@Override
	public String getLastIntervalDurationMs(){
		if(getFromTracker().getLastIntervalDurationMs() != 0){
			return Long.toString(getFromTracker().getLastIntervalDurationMs())+" ms";
		}
		return " - ";
	}

	@Override
	public String getLastExecutionDurationMs(){
		if(getFromTracker().getLastExecutionDurationMs() != -1){
			return Long.toString(getFromTracker().getLastExecutionDurationMs())+" ms";
		}
		return " - ";
	}
	
	@Override
	public boolean getIsCustom(){
		TriggerInfo triggerInfo = getFromTracker();
		return triggerInfo.isCustom();
	}
	
	@Override
	public double getPercentageOfSuccess(){
		if(getFromTracker().getNumberOfSuccesses() == 0 
				&& getFromTracker().getNumberOfErrors() == 0){
			return 0;
		}
		double percentage = (double)Math.round((double)(10000*getFromTracker().getNumberOfSuccesses())
				/(getFromTracker().getNumberOfSuccesses()
						+getFromTracker().getNumberOfErrors()))/100;
		return percentage;
	}
	
	@Override
	public boolean getIsDisabled(){
		return getFromTracker().isDisabled();
	}
	
	@Override
	public void disableJob(){
		getFromTracker().setDisabled(true);
	}
	
	@Override
	public void enableJob(){
		getFromTracker().setDisabled(false);
	}
	
	@Override
	public CronExpression getTrigger(){
		return getDefaultTrigger();
	}
	
	protected TriggerInfo getFromTracker(){
		return scheduler.getTracker().get(getClass());
	}
	
	@Override
	public CronExpression getDefaultTrigger() {
		try {
			return new CronExpression(scheduler.getTriggerGroup().getJobClasses().get(getClass()));
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	@Override
	public int compareTo(Job that){
		if(that==null){ return 1; }
		int diff = ComparableTool.nullFirstCompareTo(getJobCategory(), that.getJobCategory());
		if(diff != 0){ return diff; }
		return ComparableTool.nullFirstCompareTo(getClass().getCanonicalName(), that.getClass().getCanonicalName());
	}
	
	@Override
	public LongRunningTaskTracker getLongRunningTaskTracker(){
		return tracker;
	}
	
	@Override
	public void setTriggerTime(Date triggerTime){
		this.triggerTime = triggerTime;
	}
}
