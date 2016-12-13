package com.hotpads.joblet.execute;

import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.joblet.JobletCounters;
import com.hotpads.joblet.queue.JobletRequestQueueManager;
import com.hotpads.joblet.setting.JobletSettings;
import com.hotpads.joblet.type.JobletType;

@Singleton
public class JobletProcessorFactory{
	@Inject
	private JobletSettings jobletSettings;
	@Inject
	private JobletRequestQueueManager jobletRequestQueueManager;
	@Inject
	private JobletCallableFactory jobletCallableFactory;
	@Inject
	private JobletCounters jobletCounters;

	public JobletProcessor create(AtomicLong idGenerator, JobletType<?> jobletType){
		return new JobletProcessor(jobletSettings, jobletRequestQueueManager, jobletCallableFactory, jobletCounters,
				idGenerator, jobletType);
	}
}