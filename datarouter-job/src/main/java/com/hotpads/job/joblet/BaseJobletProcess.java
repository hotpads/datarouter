package com.hotpads.job.joblet;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.hotpads.config.job.databean.Joblet;
import com.hotpads.config.job.databean.JobletData;
import com.hotpads.util.core.stream.StreamTool;

public abstract class BaseJobletProcess implements JobletProcess{

	protected Joblet joblet;
	protected JobletData jobletData;

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
	
	public static List<Joblet> getJoblets(Collection<? extends JobletProcess> jobletProcesses){
		return StreamTool.stream(jobletProcesses)
				.map( jobletProcess -> jobletProcess.getJoblet())
				.collect(Collectors.toList());		
	}
}
