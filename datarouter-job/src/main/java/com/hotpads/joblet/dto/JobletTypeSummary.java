package com.hotpads.joblet.dto;

import java.util.Collection;

import com.hotpads.joblet.enums.JobletType;
import com.hotpads.joblet.execute.ParallelJobletProcessor;
import com.hotpads.joblet.execute.v2.JobletProcessorV2;

public class JobletTypeSummary{
	private final JobletType<?> jobletTypeEnum;
	private final int numThreads;
	private final int numRunning;

	public JobletTypeSummary(ParallelJobletProcessor processor){
		this.jobletTypeEnum = processor.getJobletType();
		this.numThreads = processor.getThreadCountFromSettings();
		this.numRunning = processor.getRunningJobletExecutorThreads().size();
	}

	public JobletTypeSummary(JobletProcessorV2 processor){
		this.jobletTypeEnum = processor.getJobletType();
		this.numThreads = processor.getThreadCountFromSettings();
		this.numRunning = processor.getNumRunningJoblets();
	}

	public static int getTotalThreads(Collection<JobletTypeSummary> dtos){
		return dtos.stream().mapToInt(JobletTypeSummary::getNumThreads).sum();
	}

	public static int getTotalRunning(Collection<JobletTypeSummary> dtos){
		return dtos.stream().mapToInt(JobletTypeSummary::getNumRunning).sum();
	}

	public static long getTotalRunningCpuPermits(Collection<JobletTypeSummary> dtos){
		return dtos.stream().mapToLong(JobletTypeSummary::getNumRunningCpuPermits).sum();
	}

	public static long getTotalRunningMemoryPermits(Collection<JobletTypeSummary> dtos){
		return dtos.stream().mapToLong(JobletTypeSummary::getNumRunningMemoryPermits).sum();
	}

	public long getNumRunningCpuPermits(){
		return jobletTypeEnum.getCpuPermits() * numRunning;
	}

	public long getNumRunningMemoryPermits(){
		return jobletTypeEnum.getMemoryPermits() * numRunning;
	}

	public String getJobletType(){
		return jobletTypeEnum.getPersistentString();
	}

	public int getNumThreads(){
		return numThreads;
	}

	public int getNumRunning(){
		return numRunning;
	}
}