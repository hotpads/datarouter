package com.hotpads.joblet;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.hotpads.joblet.execute.JobletProcessors;
import com.hotpads.joblet.execute.ParallelJobletProcessors;
import com.hotpads.joblet.execute.v2.JobletProcessorsV2;
import com.hotpads.joblet.setting.JobletSettings;

@Singleton
public class JobletProcessorsProvider implements Provider<JobletProcessors>{

	@Inject
	private JobletSettings jobletSettings;
	@Inject
	private ParallelJobletProcessors parallelJobletProcessors;
	@Inject
	private JobletProcessorsV2 jobletProcessorsV2;

	@Override
	public JobletProcessors get(){
		int version = jobletSettings.processorVersion.getValue();
		if(1 == version){
			return parallelJobletProcessors;
		}else if(2 == version){
			return jobletProcessorsV2;
		}else{
			throw new IllegalArgumentException("unknown version:" + version);
		}
	}

}
