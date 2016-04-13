package com.hotpads.joblet;

import com.hotpads.joblet.databean.JobletRequest;

public abstract class BaseJoblet<T> implements Joblet<T>{

	protected T params;
	protected JobletRequest joblet;

	public void marshallDataAndCounts(T params){
		marshallData(params);
		joblet.setNumItems(calculateNumItems(params));
		joblet.setNumTasks(calculateNumTasks(params));
	}

	@Override
	public int calculateNumTasks(T params){
		return calculateNumItems(params);//usually the same, but feel free to override
	}

	@Override
	public JobletRequest getJobletRequest(){
		return joblet;
	}

	@Override
	public void setJoblet(JobletRequest joblet){
		this.joblet = joblet;
	}

	@Override
	public void setJobletParams(T params){
		this.params = params;
	}
}
