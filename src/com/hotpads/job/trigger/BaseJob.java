package com.hotpads.job.trigger;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.quartz.CronExpression;

import com.hotpads.job.setting.thread.JobExecutorProvider.JobExecutor;
import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.ExceptionTool;
import com.hotpads.util.datastructs.MutableBoolean;

public abstract class BaseJob implements Job{
	private static Logger baseJobLogger = Logger.getLogger(BaseJob.class);
	protected Logger logger = Logger.getLogger(getClass());//for subclasses to use

	protected JobScheduler scheduler;
	protected ScheduledExecutorService executor;
	protected JobSettings jobSettings;
	protected boolean isAlreadyScheduled;
	protected MutableBoolean interrupted = new MutableBoolean(false);


	/************************* constructors *******************/

	@Inject
	public BaseJob() {
	}


	/************************ injected setters ******************/

	@Override @Inject
	public void setScheduler(JobScheduler scheduler) {
		this.scheduler = scheduler;
	}	

	@Override @Inject
	public void setExecutor(@JobExecutor ScheduledExecutorService executor) {
		this.executor = executor;
	}

	@Override @Inject
	public void setJobSettings(JobSettings jobSettings) {
		this.jobSettings = jobSettings;
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
		Job nextJobInstance = scheduler.getJobInstance(getClass());
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

	@Override
	public void runInternal() throws RuntimeException{
		if(shouldRunInternal()){
			scheduler.getTracker().get(this.getClass()).setRunning(true);
			scheduler.getTracker().get(this.getClass()).setJob(this);
			long startTimeMs = System.currentTimeMillis();
			run();
			long endTimeMs = System.currentTimeMillis();
			long durationMs = endTimeMs - startTimeMs;
			scheduler.getTracker().get(this.getClass()).setLastExecutionDurationMs(durationMs);
			scheduler.getTracker().get(this.getClass()).incrementNumberOfSuccesses();
			baseJobLogger.warn("Finished "+getClass().getSimpleName()+" in "+durationMs+"ms");
		}else{
//			baseJobLogger.warn(getClass()+" shouldRun=false");
		}
	}
	
	protected boolean shouldRunInternal(){
		return jobSettings.getProcessJobs().getValue() && shouldRun() && BooleanTool.isFalse(getIsDisabled());
	}

	protected void assertBaseServicesSet(){
		if(scheduler==null || executor==null || jobSettings==null){
			baseJobLogger.error("you must call job.setScheduler(..), job.setExecutor(..), and job.setSettings(..) if"
					+" manually instantiating this job.  It is recommended to get an instance from the Injector instead.");
		}
		if(scheduler==null){
			throw new NullPointerException("please call job.setScheduler(JobScheduler scheduler)");
		}
		if(executor==null){
			throw new NullPointerException("please call job.setExecutor(ScheduledExecutorService executor)");
		}
		if(jobSettings==null){
			throw new NullPointerException("please call job.setJobSettings(JobSettings jobSettings)");
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
		return getFromTracker().isCustom();
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
	public void interrupt(){
		if(getFromTracker().isRunning()){
			interrupted.set(true);
		}
	}
	
	@Override
	public boolean isInterrupted(){
		return interrupted.isTrue();
	}
	
	protected TriggerInfo getFromTracker(){
		return scheduler.getTracker().get(getClass());
	}
	
}
