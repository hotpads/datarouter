package com.hotpads.joblet.execute;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.joblet.enums.JobletType;

@Singleton
public class JobletExecutorThreadPoolFactory{
	@Inject
	private JobletExecutorThreadFactory jobletExecutorThreadFactory;

	public JobletExecutorThreadPool create(Integer threadPoolSize, JobletType<?> jobletType){
		return new JobletExecutorThreadPool(threadPoolSize, jobletType, jobletExecutorThreadFactory);
	}
}