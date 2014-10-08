package com.hotpads.job.trigger;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class TriggerInfo{

	
	/******************fields******************/
	
	protected Date nextScheduled;
	protected Date lastFired;
	protected Date lastThrownError;
	protected Date lastErrorTime;
	protected long lastIntervalDurationMs;
	protected long lastExecutionDurationMs;
	protected boolean isCustom;
	protected boolean isDisabled;
	protected AtomicBoolean isRunning;
	protected int numberOfSuccesses;
	protected int numberOfErrors; 
	protected Job job;
	
	
	/******************constructors******************/
	
	public TriggerInfo(){
		this.lastFired = null;
		this.lastExecutionDurationMs = -1;
		this.lastErrorTime = null;
		this.isCustom = false;
		this.isDisabled = false;
		this.isRunning = new AtomicBoolean(false);
		this.numberOfSuccesses = 0;
		this.numberOfErrors = 0;
	}
	
	
	/******************methods******************/
	
	public void incrementNumberOfSuccesses(){
		this.numberOfSuccesses++;
	}
	
	public void incrementNumberOfErrors(){
		this.numberOfErrors++;
	}

	
	/******************setters/getters******************/
	public Job getJob(){
		return job;
	}
	
	public void setJob(Job job){
		this.job = job;
	}
	
	public Date getNextScheduled(){
		return nextScheduled;
	}

	public void setNextScheduled(Date nextScheduled){
		this.nextScheduled = nextScheduled;
	}

	public Date getLastFired(){
		return lastFired;
	}

	public void setLastFired(Date lastFired){
		this.lastFired = lastFired;
	}

	public Date getLastThrownError(){
		return lastThrownError;
	}

	public void setLastThrownError(Date lastThrownError){
		this.lastThrownError = lastThrownError;
	}

	public Date getLastErrorTime(){
		return lastErrorTime;
	}

	public void setLastErrorTime(Date lastErrorTime){
		this.lastErrorTime = lastErrorTime;
	}

	public long getLastIntervalDurationMs(){
		return lastIntervalDurationMs;
	}

	public void setLastIntervalDurationMs(long lastIntervalDurationMs){
		this.lastIntervalDurationMs = lastIntervalDurationMs;
	}

	public long getLastExecutionDurationMs(){
		return lastExecutionDurationMs;
	}

	public void setLastExecutionDurationMs(long lastExecutionDurationMs){
		this.lastExecutionDurationMs = lastExecutionDurationMs;
	}

	public boolean isCustom(){
		return isCustom;
	}

	public void setCustom(boolean isCustom){
		this.isCustom = isCustom;
	}

	public boolean isDisabled(){
		return isDisabled;
	}

	public void setDisabled(boolean isDisabled){
		this.isDisabled = isDisabled;
	}

	public boolean isRunning(){
		return isRunning.get();
	}

	public void setRunning(boolean isRunning){
		this.isRunning.set(isRunning);
	}

	public int getNumberOfSuccesses(){
		return numberOfSuccesses;
	}

	public void setNumberOfSuccesses(int numberOfSuccesses){
		this.numberOfSuccesses = numberOfSuccesses;
	}

	public int getNumberOfErrors(){
		return numberOfErrors;
	}

	public void setNumberOfErrors(int numberOfErrors){
		this.numberOfErrors = numberOfErrors;
	}
	
	
}
