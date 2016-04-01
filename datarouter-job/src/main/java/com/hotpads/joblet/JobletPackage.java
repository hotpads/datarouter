package com.hotpads.joblet;

import com.hotpads.joblet.databean.JobletRequest;
import com.hotpads.joblet.databean.JobletData;

public class JobletPackage {

	private final JobletRequest joblet;
	private final JobletData jobletData;

	public JobletPackage(JobletRequest joblet, JobletData jobletData) {
		this.joblet = joblet;
		this.jobletData = jobletData;
	}

	public JobletRequest getJoblet() {
		return joblet;
	}

	public JobletData getJobletData(){
		return jobletData;
	}

}
