package com.hotpads.joblet;

import com.hotpads.joblet.databean.JobletRequest;

public abstract class BaseJoblet<T> implements Joblet<T>{

	protected T params;
	protected JobletRequest joblet;


	@Override
	public JobletRequest getJobletRequest(){
		return joblet;
	}

	@Override
	public void setJobletRequest(JobletRequest joblet){
		this.joblet = joblet;
	}

	@Override
	public void setJobletParams(T params){
		this.params = params;
	}
}
