package com.hotpads.joblet.execute.v2;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;
import com.hotpads.joblet.execute.ParallelJobletProcessor.ParallelJobletProcessorFactory;

@Singleton
public class JobletProcessorsV2 {

	//injected
	private final ParallelJobletProcessorFactory parallelJobletProcessorFactory;
	private final JobletTypeFactory jobletTypeFactory;

	private final Map<JobletType<?>,JobletProcessorV2> processorByType;


	@Inject
	public JobletProcessorsV2(ParallelJobletProcessorFactory parallelJobletProcessorFactory,
			JobletTypeFactory jobletTypeFactory){
		this.parallelJobletProcessorFactory = parallelJobletProcessorFactory;
		this.jobletTypeFactory = jobletTypeFactory;
		this.processorByType = jobletTypeFactory.getAllTypes().stream()
				.map(parallelJobletProcessorFactory::create)
				.collect(Collectors.toMap(JobletProcessorV2::getJobletType, Function.identity()));
	}

	public Map<JobletType<?>,JobletProcessorV2> getMap(){
		return processorByType;
	}

	public List<JobletExecutorThread> getCurrentlyRunningJobletExecutorThreads(){
		return processorByType.values().stream()
				.map(JobletProcessorV2::getRunningJobletExecutorThreads)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	public List<JobletExecutorThread> getCurrentlyWaitingJobletExecutorThreads(){
		return processorByType.values().stream()
				.map(JobletProcessorV2::getWaitingJobletExecutorThreads)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	public void killThread(long threadId) {
		getCurrentlyRunningJobletExecutorThreads().stream()
				.filter(thread -> thread.getId() == threadId)
				.findAny()
				.ifPresent(thread -> thread.interruptMe(true));
	}

	public void restartExecutor(int jobletTypeCode){
		JobletType<?> jobletType = jobletTypeFactory.fromPersistentInt(jobletTypeCode);
		processorByType.get(jobletType).requestShutdown();
		processorByType.put(jobletType, parallelJobletProcessorFactory.create(jobletType));
	}

	public void requestShutdown(){
		processorByType.values().forEach(JobletProcessorV2::requestShutdown);
	}

}
