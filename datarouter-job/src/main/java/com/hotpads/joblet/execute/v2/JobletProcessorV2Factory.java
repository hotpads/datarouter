package com.hotpads.joblet.execute.v2;

import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.queue.JobletRequestQueueManager;
import com.hotpads.joblet.setting.JobletSettings;

@Singleton
public class JobletProcessorV2Factory{
	@Inject
	private JobletSettings jobletSettings;
	@Inject
	private JobletRequestQueueManager jobletRequestQueueManager;
	@Inject
	private JobletCallableFactory jobletCallableFactory;

	public JobletProcessorV2 create(AtomicLong idGenerator, JobletType<?> jobletType){
		return new JobletProcessorV2(jobletSettings, jobletRequestQueueManager, jobletCallableFactory, idGenerator,
				jobletType);
	}
}