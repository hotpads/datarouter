package com.hotpads.joblet.enums;

import com.hotpads.joblet.Joblet;

public class BaseJobletType<P> implements JobletType<P>{
	private final String persistentString;
	private final Class<? extends Joblet<P>> clazz;
	private final boolean rateLimited;
	private final Integer batchSize;
	private final Integer cpuPermits;
	private final Integer memoryPermits;
	private final boolean causesScaling;

	public BaseJobletType(String persistentString, Class<? extends Joblet<P>> clazz, boolean rateLimited,
			Integer batchSize, Integer cpuPermits, Integer memoryPermits, boolean causesScaling){
		this.persistentString = persistentString;
		this.clazz = clazz;
		this.rateLimited = rateLimited;
		this.batchSize = batchSize;
		this.cpuPermits = cpuPermits;
		this.memoryPermits = memoryPermits;
		this.causesScaling = causesScaling;
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public Class<? extends Joblet<P>> getAssociatedClass(){
		return clazz;
	}

	@Override
	public boolean getRateLimited(){
		return rateLimited;
	}

	@Override
	public Integer getBatchSize(){
		return batchSize;
	}

	@Override
	public Integer getCpuPermits(){
		return cpuPermits;
	}

	@Override
	public Integer getMemoryPermits(){
		return memoryPermits;
	}

	@Override
	public boolean causesScaling(){
		return causesScaling;
	}
}
