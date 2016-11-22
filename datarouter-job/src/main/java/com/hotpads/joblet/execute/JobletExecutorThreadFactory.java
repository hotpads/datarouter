package com.hotpads.joblet.execute;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.joblet.JobletCounters;
import com.hotpads.joblet.JobletFactory;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletService;
import com.hotpads.joblet.enums.JobletTypeFactory;

@Singleton
public class JobletExecutorThreadFactory{
	@Inject
	private JobletTypeFactory jobletTypeFactory;
	@Inject
	private JobletFactory jobletFactory;
	@Inject
	private JobletNodes jobletNodes;
	@Inject
	private JobletService jobletService;
	@Inject
	private JobletCounters jobletCounters;

	public JobletExecutorThread create(JobletExecutorThreadPool jobletExecutorThreadPool, ThreadGroup threadGroup){
		return new JobletExecutorThread(jobletExecutorThreadPool, threadGroup, jobletTypeFactory, jobletFactory,
				jobletNodes, jobletService, jobletCounters);
	}
}