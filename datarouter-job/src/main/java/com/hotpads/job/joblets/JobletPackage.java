package com.hotpads.job.joblets;

import com.hotpads.config.job.databean.Joblet;
import com.hotpads.config.job.databean.JobletData;

public class JobletPackage {

	private final Joblet joblet;
	private final JobletData jobletData;

	public JobletPackage(Joblet joblet, JobletData jobletData) {
		this.joblet = joblet;
		this.jobletData = jobletData;
	}

	public Joblet getJoblet() {
		return joblet;
	}

	public JobletData getJobletData(){
		return jobletData;
	}

}
