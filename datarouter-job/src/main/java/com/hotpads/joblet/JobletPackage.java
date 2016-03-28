package com.hotpads.joblet;

import com.hotpads.joblet.databean.Joblet;
import com.hotpads.joblet.databean.JobletData;

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
