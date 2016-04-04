package com.hotpads.joblet;

import com.hotpads.joblet.databean.JobletData;
import com.hotpads.joblet.databean.JobletRequest;

public abstract class BaseJoblet<T> implements Joblet<T>{

	protected JobletRequest joblet;
	protected JobletData jobletData = new JobletData(null);

	public void marshallDataAndCounts(T params){
		marshallData(params);
		joblet.setNumItems(calculateNumItems());
		joblet.setNumTasks(calculateNumTasks());
	}

	@Override
	public int calculateNumTasks(){
		return calculateNumItems();//usually the same, but feel free to override
	}

	@Override
    public void unmarshallDataIfNotAlready(String encodedParams) {
		if(!getUnmarshalled()){
			unmarshallData(encodedParams);
		}
	}

	public boolean getUnmarshalled(){
		return joblet.getUnmarshalled();
	}

	public void setUnmarshalled(boolean unmarshalled){
		joblet.setUnmarshalled(unmarshalled);
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
	public JobletData getJobletData(){
		return jobletData;
	}

	@Override
	public void setJobletData(JobletData jobletData){
		this.jobletData = jobletData;
	}

	@Override
	public void setData(String data){
		jobletData.setData(data);
	}

	@Override
	public String getData(){
		return jobletData.getData();
	}

}
