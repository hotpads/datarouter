package com.hotpads.joblet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.config.job.enums.HotPadsJobletType;

//static map to hold a ParallelJobletProcessor for each joblet type
@Singleton
public class ParallelJobletProcessors {
	private static final Logger logger = LoggerFactory.getLogger(ParallelJobletProcessors.class);

	private final Map<JobletType<?>,ParallelJobletProcessor> processorsByJobletType = new ConcurrentHashMap<>();
	private final ParallelJobletProcessorFactory parallelJobletProcessorFactory;

	@Inject
	public ParallelJobletProcessors(ParallelJobletProcessorFactory parallelJobletProcessorFactory){
		this.parallelJobletProcessorFactory = parallelJobletProcessorFactory;
		for(JobletType<?> jobletType : HotPadsJobletType.values()){
			processorsByJobletType.put(jobletType, parallelJobletProcessorFactory.create(jobletType));
		}
		logger.info("created ParallelJobletProcessors singleton with " + processorsByJobletType.keySet());
	}

	public Map<JobletType<?>,ParallelJobletProcessor> getMap(){
		return processorsByJobletType;
	}

	public List<JobletExecutorThread> getCurrentlyRunningJobletExecutorThreads(){
		ArrayList<JobletExecutorThread> runningJobletThreads = new ArrayList<>();
		for(ParallelJobletProcessor processor : getMap().values()){
			runningJobletThreads.addAll(processor.getRunningJobletExecutorThreads());
		}
		return runningJobletThreads;
	}

	public List<JobletExecutorThread> getCurrentlyWaitingJobletExecutorThreads(){
		ArrayList<JobletExecutorThread> waitingJobletThreads = new ArrayList<>();
		for(ParallelJobletProcessor processor : getMap().values()){
			waitingJobletThreads.addAll(processor.getWaitingJobletExecutorThreads());
		}
		return waitingJobletThreads;
	}

	public void killThread(long threadId) {
		for(JobletExecutorThread thread : getCurrentlyRunningJobletExecutorThreads()){
			if(thread.getId() == threadId){
				thread.killMe(true);
				return;
			}
		}
	}

	public void restartExecutor(String jobletTypeString){
		JobletType<?> jobletType = HotPadsJobletType.fromString(jobletTypeString);
		getMap().get(jobletType).stop();
		getMap().put(jobletType, parallelJobletProcessorFactory.create(jobletType));
	}

}
