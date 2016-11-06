package com.hotpads.joblet.execute.v2;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;

@Singleton
public class JobletProcessorsV2 {

	private final Map<JobletType<?>,JobletProcessorV2> processorByType;


	@Inject
	public JobletProcessorsV2(JobletProcessorV2Factory jobletProcessorV2Factory, JobletTypeFactory jobletTypeFactory){
		this.processorByType = jobletTypeFactory.getAllTypes().stream()
				.map(jobletProcessorV2Factory::create)
				.collect(Collectors.toMap(JobletProcessorV2::getJobletType, Function.identity()));
	}


	public void requestShutdown(){
		processorByType.values().forEach(JobletProcessorV2::requestShutdown);
	}

}
