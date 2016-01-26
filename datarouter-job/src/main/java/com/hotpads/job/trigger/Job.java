package com.hotpads.job.trigger;

import java.util.Date;
import java.util.concurrent.Callable;

import com.hotpads.job.record.LongRunningTaskTracker;
import com.hotpads.util.core.date.CronExpression;

public interface Job extends Callable<Void>, Comparable<Job>{

	CronExpression getTrigger();
	Long getDelayBeforeNextFireTimeMs();
	void scheduleNextRun(boolean immediate);
	boolean shouldRun();
	void run() throws Exception;
	void runInternal() throws Exception;

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

	LongRunningTaskTracker getLongRunningTaskTracker();
	void trackBeforeRun(Long starttime);
	void trackAfterRun(Long endTime);
	void setTriggerTime(Date triggerTime);
}
