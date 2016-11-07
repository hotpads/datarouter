package com.hotpads.joblet.execute.v2;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.joblet.dto.JobletTypeSummary;
import com.hotpads.joblet.dto.RunningJoblet;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;
import com.hotpads.joblet.execute.JobletProcessors;

@Singleton
public class JobletProcessorsV2 implements JobletProcessors{

	private final JobletProcessorV2Factory jobletProcessorV2Factory;
	private final JobletTypeFactory jobletTypeFactory;

	private Map<JobletType<?>,JobletProcessorV2> processorByType;


	@Inject
	public JobletProcessorsV2(JobletProcessorV2Factory jobletProcessorV2Factory, JobletTypeFactory jobletTypeFactory){
		this.jobletProcessorV2Factory = jobletProcessorV2Factory;
		this.jobletTypeFactory = jobletTypeFactory;
	}


	@Override
	public void createAndStartProcessors(){
		this.processorByType = jobletTypeFactory.getAllTypes().stream()
				.map(jobletProcessorV2Factory::create)
				.collect(Collectors.toMap(JobletProcessorV2::getJobletType, Function.identity()));
	}

	@Override
	public void requestShutdown(){
		processorByType.values().forEach(JobletProcessorV2::requestShutdown);
	}

	@Override
	public Map<JobletType<?>,List<RunningJoblet>> getRunningJobletsByType(){
		return processorByType.values().stream()
				.filter(processor -> DrCollectionTool.notEmpty(processor.getRunningJoblets()))
				.collect(Collectors.toMap(JobletProcessorV2::getJobletType, JobletProcessorV2::getRunningJoblets));
	}

	@Override
	public List<JobletTypeSummary> getTypeSummaries(){
		return processorByType.values().stream()
				.map(JobletTypeSummary::new)
				.filter(summary -> summary.getNumRunning() > 0)
				.sorted(Comparator.comparing(JobletTypeSummary::getJobletType))
				.collect(Collectors.toList());
	}

}
