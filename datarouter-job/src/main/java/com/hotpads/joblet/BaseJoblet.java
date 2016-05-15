package com.hotpads.joblet;

import com.hotpads.joblet.databean.JobletRequest;

public abstract class BaseJoblet<T> implements Joblet<T>{

	protected T params;
	protected JobletRequest jobletRequest;


	@Override
	public JobletRequest getJobletRequest(){
		return jobletRequest;
	}

	@Override
	public void setJobletRequest(JobletRequest jobletRequest){
		this.jobletRequest = jobletRequest;
	}

	@Override
	public void setJobletParams(T params){
		this.params = params;
	}
}
