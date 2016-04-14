package com.hotpads.joblet;

import com.hotpads.joblet.databean.JobletRequest;

public interface Joblet<P>
extends JobletCodec<P>{

	JobletRequest getJobletRequest();
	void setJoblet(JobletRequest jobletRequest);

	void setJobletParams(P params);
	Long process();

}
