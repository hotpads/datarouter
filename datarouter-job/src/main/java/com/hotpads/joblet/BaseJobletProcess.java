package com.hotpads.joblet;

import com.hotpads.joblet.databean.Joblet;
import com.hotpads.joblet.databean.JobletData;

public abstract class BaseJobletProcess implements JobletProcess{

	protected Joblet joblet;
	protected JobletData jobletData = new JobletData();

	public void marshallDataAndCounts(){
		marshallData();
		joblet.setNumItems(calculateNumItems());
		joblet.setNumTasks(calculateNumTasks());
	}

	@Override
	public int calculateNumTasks(){
		return calculateNumItems();//usually the same, but feel free to override
	}

	@Override
    public void unmarshallDataIfNotAlready() {
		if(!getUnmarshalled()){
			unmarshallData();
		}
	}

	public boolean getUnmarshalled(){
		return joblet.getUnmarshalled();
	}

	public void setUnmarshalled(boolean unmarshalled){
		joblet.setUnmarshalled(unmarshalled);
	}

	@Override
	public Joblet getJoblet(){
		return joblet;
	}

	@Override
	public void setJoblet(Joblet joblet){
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