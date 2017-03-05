package com.hotpads.joblet.dto;

import java.util.Date;
import java.util.Optional;

import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.joblet.JobletPackage;
import com.hotpads.joblet.type.JobletType;

public class RunningJoblet{

	private String name;
	private String id;
	private Date startedAt;
	private String queueId;
	private String jobletData;

	public RunningJoblet(JobletType<?> jobletType, long id, Date startedAt, Optional<JobletPackage> jobletPackage){
		this.name = jobletType.getPersistentString();
		this.id = Long.toString(id);
		this.startedAt = startedAt;
		if(jobletPackage.isPresent()){
			this.queueId = jobletPackage.get().getJobletRequest().getQueueId();
			this.jobletData = jobletPackage.get().getJobletData().getData();
		}
	}


	public boolean hasPayload(){
		return jobletData != null;
	}

	public String getName(){
		return name;
	}

	public String getId(){
		return id;
	}

	public String getRunningTimeString(){
		return DrDateTool.getAgoString(startedAt);
	}

	public String getQueueId(){
		return queueId;
	}

	public String getJobletData(){
		return jobletData;
	}

}
