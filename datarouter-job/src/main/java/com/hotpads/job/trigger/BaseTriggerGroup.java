package com.hotpads.job.trigger;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.hotpads.job.web.JobCategory;
import com.hotpads.job.web.TriggersRepository.JobPackage;
import com.hotpads.util.core.date.CronExpression;

public abstract class BaseTriggerGroup implements TriggerGroup{

	private final List<JobPackage> jobPackages;

	public BaseTriggerGroup(){
		this.jobPackages = new ArrayList<>();
	}

	@Override
	public List<JobPackage> makeJobPackages(){
		if(jobPackages.isEmpty()){
			registerTriggers();
		}
		return jobPackages;
	}

	protected void register(JobCategory jobCategory, String cronExpression, Class<? extends Job> jobClass){
		try{
			jobPackages.add(new JobPackage(jobCategory, new CronExpression(cronExpression), jobClass));
		}catch(ParseException e){
			throw new RuntimeException(e);
		}
	}

	protected abstract void registerTriggers();

}
