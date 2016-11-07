package com.hotpads.joblet.execute;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.joblet.dto.RunningJoblet;
import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.enums.JobletTypeFactory;
import com.hotpads.joblet.execute.ParallelJobletProcessor.ParallelJobletProcessorFactory;

@Singleton
public class ParallelJobletProcessors implements JobletProcessors{

	//injected
	private final ParallelJobletProcessorFactory parallelJobletProcessorFactory;
	private final JobletTypeFactory jobletTypeFactory;

	private Map<JobletType<?>,ParallelJobletProcessor> processorByType;


	@Inject
	public ParallelJobletProcessors(ParallelJobletProcessorFactory parallelJobletProcessorFactory,
			JobletTypeFactory jobletTypeFactory){
		this.parallelJobletProcessorFactory = parallelJobletProcessorFactory;
		this.jobletTypeFactory = jobletTypeFactory;
	}

	@Override
	public void createAndStartProcessors(){
		processorByType = jobletTypeFactory.getAllTypes().stream()
				.map(parallelJobletProcessorFactory::create)
				.collect(Collectors.toMap(ParallelJobletProcessor::getJobletType, Function.identity()));
	}

	@Override
	public Map<JobletType<?>,ParallelJobletProcessor> getMap(){
		return processorByType;
	}

	@Override
	public List<JobletExecutorThread> getCurrentlyRunningJobletExecutorThreads(){
		return processorByType.values().stream()
				.map(ParallelJobletProcessor::getRunningJobletExecutorThreads)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	@Override
	public List<JobletExecutorThread> getCurrentlyWaitingJobletExecutorThreads(){
		return processorByType.values().stream()
				.map(ParallelJobletProcessor::getWaitingJobletExecutorThreads)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	@Override
	public void killThread(long threadId) {
		getCurrentlyRunningJobletExecutorThreads().stream()
				.filter(thread -> thread.getId() == threadId)
				.findAny()
				.ifPresent(thread -> thread.interruptMe(true));
	}

	@Override
	public void restartExecutor(int jobletTypeCode){
		JobletType<?> jobletType = jobletTypeFactory.fromPersistentInt(jobletTypeCode);
		processorByType.get(jobletType).requestShutdown();
		processorByType.put(jobletType, parallelJobletProcessorFactory.create(jobletType));
	}

	@Override
	public void requestShutdown(){
		processorByType.values().forEach(ParallelJobletProcessor::requestShutdown);
	}

	@Override
	public Map<JobletType<?>,List<RunningJoblet>> getRunningJobletsByType(){
		return processorByType.values().stream()
				.filter(processor -> DrCollectionTool.notEmpty(processor.getRunningJoblets()))
				.collect(Collectors.toMap(ParallelJobletProcessor::getJobletType,
						ParallelJobletProcessor::getRunningJoblets));
	}

}
