package com.hotpads.joblet;

public interface JobletScheduler {

	void blockUntilReadyForNewJoblet();
	void submitJoblet(JobletPackage jobletPackage);

}
