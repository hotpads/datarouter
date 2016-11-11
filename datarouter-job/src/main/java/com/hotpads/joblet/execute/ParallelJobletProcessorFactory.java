package com.hotpads.joblet.execute;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.joblet.JobletService;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.setting.JobletSettings;

@Singleton
public class ParallelJobletProcessorFactory{
	@Inject
	private JobletService jobletService;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private JobletSettings jobletSettings;
	@Inject
	private JobletExecutorThreadPoolFactory jobletExecutorThreadPoolFactory;

	public ParallelJobletProcessor create(JobletType<?> jobletType){
		return new ParallelJobletProcessor(datarouterProperties, jobletSettings, jobletService,
				jobletExecutorThreadPoolFactory, jobletType);
	}
}