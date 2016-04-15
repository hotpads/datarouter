package com.hotpads.joblet.enums;

import java.util.Objects;

import com.hotpads.joblet.Joblet;

public class JobletType<P>{
	private final String persistentString;
	private final Class<? extends Joblet<P>> clazz;
	private final boolean rateLimited;
	private final Integer batchSize;
	private final Integer cpuPermits;
	private final Integer memoryPermits;
	private final boolean causesScaling;

	public JobletType(String persistentString, Class<? extends Joblet<P>> clazz, boolean rateLimited,
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
	public boolean equals(Object other){
		return this == other; //assumes same instance is always used
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(this);
	}

	public String getDisplay(){
		return getPersistentString();
	}

	/*-------------- get -------------------*/

	public String getPersistentString(){
		return persistentString;
	}

	public Class<? extends Joblet<P>> getAssociatedClass(){
		return clazz;
	}

	public boolean getRateLimited(){
		return rateLimited;
	}

	public Integer getBatchSize(){
		return batchSize;
	}

	public Integer getCpuPermits(){
		return cpuPermits;
	}

	public Integer getMemoryPermits(){
		return memoryPermits;
	}

	public boolean causesScaling(){
		return causesScaling;
	}

}
