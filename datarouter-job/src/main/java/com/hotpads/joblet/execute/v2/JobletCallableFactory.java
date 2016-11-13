package com.hotpads.joblet.execute.v2;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.joblet.JobletFactory;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletService;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.util.datastructs.MutableBoolean;

@Singleton
public class JobletCallableFactory{

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private JobletNodes jobletNodes;
	@Inject
	private JobletService jobletService;
	@Inject
	private JobletFactory jobletFactory;


	public JobletCallable create(MutableBoolean shutdownRequested, JobletProcessorV2 jobletProcessor,
			JobletType<?> jobletType, long id){
		return new JobletCallable(datarouterProperties, jobletNodes, jobletService, jobletFactory, shutdownRequested,
				jobletProcessor, jobletType, id);
	}

}
