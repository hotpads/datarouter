package com.hotpads.joblet.execute.v2;

import javax.inject.Inject;

import com.hotpads.datarouter.config.DatarouterProperties;
import com.hotpads.joblet.JobletFactory;
import com.hotpads.joblet.JobletNodes;
import com.hotpads.joblet.JobletService;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;
import com.hotpads.util.datastructs.MutableBoolean;

public class JobletCallableFactory{

	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private JobletTypeFactory jobletTypeFactory;
	@Inject
	private JobletNodes jobletNodes;
	@Inject
	private JobletService jobletService;
	@Inject
	private JobletFactory jobletFactory;


	public JobletCallable create(MutableBoolean shutdownRequested, JobletType<?> jobletType, long id){
		return new JobletCallable(datarouterProperties, jobletTypeFactory, jobletNodes, jobletService, jobletFactory,
				shutdownRequested, jobletType, id);
	}

}
