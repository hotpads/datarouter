package com.hotpads.joblet.execute;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.joblet.JobletCounters;
import com.hotpads.joblet.JobletFactory;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletService;
import com.hotpads.joblet.type.JobletType;
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
	@Inject
	private JobletCounters jobletCounters;


	public JobletCallable create(MutableBoolean shutdownRequested, JobletProcessor jobletProcessor,
			JobletType<?> jobletType, long id){
		return new JobletCallable(datarouterProperties, jobletNodes, jobletService, jobletFactory, jobletCounters,
				shutdownRequested, jobletProcessor, jobletType, id);
	}

}
