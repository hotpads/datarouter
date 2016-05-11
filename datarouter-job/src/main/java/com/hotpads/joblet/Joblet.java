package com.hotpads.joblet;

import com.hotpads.joblet.databean.JobletRequest;

public interface Joblet<P>{

	JobletRequest getJobletRequest();
	void setJobletRequest(JobletRequest jobletRequest);

	void setJobletParams(P params);
	Long process();

}
