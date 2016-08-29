package com.hotpads.joblet.execute;

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
public class ParallelJobletProcessors {

	//injected
	private final ParallelJobletProcessorFactory parallelJobletProcessorFactory;
	private final JobletTypeFactory jobletTypeFactory;

	private final Map<JobletType<?>,ParallelJobletProcessor> processorByType;


	@Inject
	public ParallelJobletProcessors(ParallelJobletProcessorFactory parallelJobletProcessorFactory,
			JobletTypeFactory jobletTypeFactory){
		this.parallelJobletProcessorFactory = parallelJobletProcessorFactory;
		this.jobletTypeFactory = jobletTypeFactory;
		this.processorByType = jobletTypeFactory.getAllTypes().stream()
				.map(parallelJobletProcessorFactory::create)
				.collect(Collectors.toMap(ParallelJobletProcessor::getJobletType, Function.identity()));
	}

	public Map<JobletType<?>,ParallelJobletProcessor> getMap(){
		return processorByType;
	}

	public List<JobletExecutorThread> getCurrentlyRunningJobletExecutorThreads(){
		return processorByType.values().stream()
				.map(ParallelJobletProcessor::getRunningJobletExecutorThreads)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	public List<JobletExecutorThread> getCurrentlyWaitingJobletExecutorThreads(){
		return processorByType.values().stream()
				.map(ParallelJobletProcessor::getWaitingJobletExecutorThreads)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	public void killThread(long threadId) {
		getCurrentlyRunningJobletExecutorThreads().stream()
				.filter(thread -> thread.getId() == threadId)
				.findAny()
				.ifPresent(thread -> thread.killMe(true));
	}

	public void restartExecutor(String jobletTypeString){
		JobletType<?> jobletType = jobletTypeFactory.fromPersistentString(jobletTypeString);
		processorByType.get(jobletType).requestShutdown();
		processorByType.put(jobletType, parallelJobletProcessorFactory.create(jobletType));
	}

	public void requestShutdown(){
		processorByType.values().forEach(ParallelJobletProcessor::requestShutdown);
	}

}
