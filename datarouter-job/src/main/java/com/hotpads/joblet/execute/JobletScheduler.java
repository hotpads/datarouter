package com.hotpads.joblet.execute;

import com.hotpads.joblet.JobletPackage;

public interface JobletScheduler {

	void blockUntilReadyForNewJoblet();
	void submitJoblet(JobletPackage jobletPackage);

}
