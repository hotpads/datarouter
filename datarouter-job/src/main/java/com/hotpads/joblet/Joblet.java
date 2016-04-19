package com.hotpads.joblet;

import com.hotpads.joblet.databean.JobletRequest;

public interface Joblet<P>{

	JobletRequest getJobletRequest();
	void setJoblet(JobletRequest jobletRequest);

	void setJobletParams(P params);
	Long process();

}
