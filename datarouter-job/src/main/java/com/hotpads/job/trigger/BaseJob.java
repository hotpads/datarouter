package com.hotpads.job.trigger;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.handler.exception.ExceptionRecorder;
import com.hotpads.job.record.JobExecutionStatus;
import com.hotpads.job.record.LongRunningTaskTracker;
import com.hotpads.job.record.LongRunningTaskType;
import com.hotpads.util.core.date.CronExpression;

public abstract class BaseJob implements Job{
	private static final Logger logger = LoggerFactory.getLogger(BaseJob.class);

	protected JobScheduler scheduler;
	protected ScheduledExecutorService executor;
	protected Setting<Boolean> processJobsSetting;
	protected AtomicBoolean isAlreadyScheduled = new AtomicBoolean(false);
	protected AtomicBoolean isAlreadyRunning = new AtomicBoolean(false);
	protected LongRunningTaskTracker tracker;
	protected Setting<Boolean> shouldSaveLongRunningTasks;
	private String serverName;
	private Date triggerTime;
	private String jobClass;
	private final Date createdAt;//timestamp at construction, before queueing in the scheduler
	private Date startedAt;//timestamp after queue, when processing begins
	private Date finishedAt;

	@Inject
	protected ExceptionRecorder exceptionRecorder;

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
		this.createdAt = new Date();
	}

	/*********************** methods ******************************/

	public String getJobClass(){
		return jobClass;
	}

	@Override
	public Long getDelayBeforeNextFireTimeMs(){
		CronExpression trigger = getTrigger();
		if(trigger == null){
			return null;
		}
		Date nextFireDate = trigger.getNextValidTimeAfter(new Date());
		if(nextFireDate==null){
			return null;
		}
		long delay = nextFireDate.getTime() - System.currentTimeMillis();
		return delay;
	}

	@Override
	public void scheduleNextRun(boolean immediate){
		Long delay;
		if(immediate){
			delay = 0L;
			logger.warn("scheduling " + getClass().getSimpleName() + " to run immediately");
		}else{
			delay = getDelayBeforeNextFireTimeMs();
			if(delay==null){
				logger.warn("couldn't schedule "+getClass()+" because no trigger defined");
				return;
			}
			if(isAlreadyScheduled.get()){
				logger.warn("couldn't schedule "+getClass()+" because is already scheduled");
				return;
			}
		}
		Job nextJobInstance = scheduler.getJobInstance(getClass());
		Long nextTriggerTime = System.currentTimeMillis() + delay;
		nextJobInstance.setTriggerTime(new Date(nextTriggerTime));
		if(isAlreadyScheduled.compareAndSet(false, true)){
			executor.schedule(nextJobInstance, delay, TimeUnit.MILLISECONDS);
			if(hasStarted()){//only log if this instance ran
				logger.info("scheduled " + nextJobInstance + " with delay " + delay + "ms");
			}
		}
	}

	@Override
	public Void call(){
		assertBaseServicesSet();
		try{
			runInternal();
		}catch(Exception e){
			scheduler.getTracker().get(this.getClass()).setRunning(false);
			getFromTracker().incrementNumberOfErrors();
			getFromTracker().setLastErrorTime(new Date());
			logger.warn("exception executing "+getClass(), e);
			tryRecordException(e);
		}finally{
			try{
				if(!isAlreadyRunning.get()){
					getFromTracker().setRunning(false);
				}else{
					logger.warn("couldn't run "+getClass()+" because it is already running");
				}
			}catch(Exception e){
				logger.warn("exception in finally block", e);
			}
			try{
				isAlreadyScheduled.set(false);
				scheduleNextRun(false);
			}catch(Exception e){
				logger.warn("exception in finally block", e);
			}
		}
		return null;
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
	public void runInternal() throws Exception{
		if(shouldRunInternal()){
			startedAt = new Date();
			scheduler.getTracker().get(this.getClass()).setRunning(true);
			scheduler.getTracker().get(this.getClass()).setJob(this);
			trackBeforeRun(startedAt.getTime());
			run();
			finishedAt = new Date();
			trackAfterRun(finishedAt.getTime());
			scheduler.getTracker().get(this.getClass()).setLastExecutionDurationMs(getElapsedRunningTimeMs());
			scheduler.getTracker().get(this.getClass()).incrementNumberOfSuccesses();
			Date nextJobTriggerTime = getTrigger().getNextValidTimeAfter(triggerTime);
			String jobCompletionLog = "Finished " + getClass().getSimpleName() + " in " + getElapsedRunningTimeMs()
					+ "ms";
			if(getStartDelayMs() > 1000) {
				jobCompletionLog += ", delayed by " + getStartDelayMs() + "ms";
			}
			if(new Date().after(nextJobTriggerTime)) {
				jobCompletionLog += ", missed next trigger";
			}
			if(getElapsedRunningTimeMs() > 100) {
				logger.warn(jobCompletionLog);
			}else{
				logger.debug(jobCompletionLog);
			}
		}else{
//			logger.warn(getClass()+" shouldRun=false");
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
			isAlreadyRunning.set(true);
			return false;
		}
		return processJobsSetting.getValue() && shouldRun() && DrBooleanTool.isFalse(getIsDisabled());
	}

	protected void assertBaseServicesSet(){
		if(scheduler==null || executor==null || processJobsSetting==null){
			logger.error("you must call job.setScheduler(..), job.setExecutor(..), and job.setSettings(..) if"
					+ " manually instantiating this job.  It is recommended to get an instance from the Injector"
					+ " instead.");
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
		if(trigger == null){
			return null;
		}
		Date nextFireDate = trigger.getNextValidTimeAfter(new Date());
		if(nextFireDate==null){
			return null;
		}
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

	public String getServerName(){
		return serverName;
	}

	public Date getCreatedAt(){
		return createdAt;
	}

	public Date getStartedAt(){
		return startedAt;
	}

	public boolean hasStarted(){
		return startedAt != null;
	}

	public long getStartDelayMs(){
		return startedAt.getTime() - triggerTime.getTime();//NPE if either is null
	}

	public long getQueuedTimeMs(){
		if(startedAt == null){
			return System.currentTimeMillis() - createdAt.getTime();
		}
		return startedAt.getTime() - createdAt.getTime();
	}

	public long getElapsedRunningTimeMs(){
		if(startedAt == null){
			return 0;
		}
		if(finishedAt == null){
			return System.currentTimeMillis() - startedAt.getTime();
		}
		return finishedAt.getTime() - startedAt.getTime();
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
		if(that==null){
			return 1;
		}
		int diff = DrComparableTool.nullFirstCompareTo(getJobCategory(), that.getJobCategory());
		if(diff != 0){
			return diff;
		}
		return DrComparableTool.nullFirstCompareTo(getClass().getCanonicalName(), that.getClass().getCanonicalName());
	}

	@Override
	public LongRunningTaskTracker getLongRunningTaskTracker(){
		return tracker;
	}

	@Override
	public void setTriggerTime(Date triggerTime){
		this.triggerTime = triggerTime;
	}

	public void tryRecordException(Exception exception){
		exceptionRecorder.tryRecordException(exception, getClass().getName(), JobExceptionCategory.JOB);
	}

}
