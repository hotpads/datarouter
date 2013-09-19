package com.hotpads.job.trigger;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;

import org.quartz.CronExpression;

public interface Job extends Callable<Void>{
	
	void setScheduler(JobScheduler scheduler);
	void setExecutor(ScheduledExecutorService executor);
	void setJobSettings(JobSettings jobSettings);

	CronExpression getTrigger();
	Long getDelayBeforeNextFireTimeMs();
	void scheduleNextRun();
	boolean shouldRun();
	void run() throws RuntimeException;
	void runInternal() throws RuntimeException;
	
	String getJobCategory();
	Date getNextScheduled();
	Date getLastFired();
	String getLastErrorTime();
	String getLastIntervalDurationMs();
	String getLastExecutionDurationMs();
	boolean getIsCustom();
	CronExpression getDefaultTrigger();	
	double getPercentageOfSuccess();
	boolean getIsDisabled();
	void disableJob();
	void enableJob();
	
	void interrupt();
	boolean isInterrupted();
}
