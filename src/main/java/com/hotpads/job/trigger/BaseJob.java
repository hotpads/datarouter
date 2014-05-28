package com.hotpads.job.trigger;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.quartz.CronExpression;

import com.hotpads.job.record.JobExecutionStatus;
import com.hotpads.job.record.LongRunningTaskTracker;
import com.hotpads.job.record.LongRunningTaskType;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.handler.exception.ExceptionHandlingConfig;
import com.hotpads.handler.exception.ExceptionHandlingFilter.ExceptionRecordNode;
import com.hotpads.handler.exception.ExceptionRecord;
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
	private static Logger baseJobLogger = Logger.getLogger(BaseJob.class);
	protected Logger logger = Logger.getLogger(getClass());//for subclasses to use

	protected JobScheduler scheduler;
	protected ScheduledExecutorService executor;
	protected Setting<Boolean> processJobsSetting;
	protected boolean isAlreadyScheduled;
	protected MutableBoolean interrupted = new MutableBoolean(false);
	protected LongRunningTaskTracker tracker;
	protected Setting<Boolean> shouldSaveLongRunningTasks;
	private String serverName;
	
	@Inject
	@ExceptionRecordNode
	@SuppressWarnings("rawtypes")
	private SortedMapStorageNode exceptionRecordNode;
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
		this.shouldSaveLongRunningTasks = jobEnvironment.getShouldSaveLongRunningTasks();
		String jobClass = this.getClass().getName();
		this.serverName = jobEnvironment.getServerName();
		this.tracker = jobEnvironment.getLongRunningTaskTrackerFactory().create(jobClass, serverName, 
				jobEnvironment.getShouldSaveLongRunningTasks(), LongRunningTaskType.job);
	}

	/*********************** methods ******************************/

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
	public void scheduleNextRun(){
		Long delay = getDelayBeforeNextFireTimeMs();
		if(delay==null){ 
			baseJobLogger.warn("couldn't schedule "+getClass()+" because no trigger defined");
			return;
		}
		if(isAlreadyScheduled){
			baseJobLogger.warn("couldn't schedule "+getClass()+" because is already scheduled");
			return;
		}
		Job nextJobInstance = scheduler.getJobInstance(getClass(), getTrigger().getCronExpression());
		Long triggerTime = System.currentTimeMillis() + delay;
		
		nextJobInstance.getLongRunningTaskTracker().getTask().setTriggerTime(new Date(triggerTime));
		if(shouldSaveLongRunningTasks.getValue()){
			nextJobInstance.getLongRunningTaskTracker().getNode().put(nextJobInstance.getLongRunningTaskTracker().getTask(), null);
		}
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
			baseJobLogger.warn("exception executing "+getClass());
			baseJobLogger.warn(ExceptionTool.getStackTraceAsString(e));
			recordException(e);
		}finally{
			try{
				getFromTracker().setRunning(false);
			}catch(Exception e){
				baseJobLogger.warn("exception in finally block");
				baseJobLogger.warn(ExceptionTool.getStackTraceAsString(e));
			}
			try{
				isAlreadyScheduled = false;
				scheduleNextRun();
			}catch(Exception e){
				baseJobLogger.warn("exception in finally block");
				baseJobLogger.warn(ExceptionTool.getStackTraceAsString(e));
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void recordException(RuntimeException e) {
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
		if(shouldSaveLongRunningTasks.getValue()){
			tracker.getNode().put(tracker.getTask(), null);
		}
	}
	
	@Override
	public void runInternal() throws RuntimeException{
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
			baseJobLogger.warn("Finished "+getClass().getSimpleName()+" in "+durationMs+"ms");
		}else{
//			baseJobLogger.warn(getClass()+" shouldRun=false");
		}
	}
	
	@Override
	public void trackAfterRun(Long endTime){
		tracker.getTask().setFinishTime(new Date(endTime));
		tracker.getTask().setJobExecutionStatus(JobExecutionStatus.success);
		if(shouldSaveLongRunningTasks.getValue()){
			tracker.getNode().put(tracker.getTask(), null);
		}
	}
	
	protected boolean shouldRunInternal(){
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
}
