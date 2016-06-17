package com.hotpads.joblet.enums;

import java.util.function.Supplier;

import com.hotpads.joblet.Joblet;
import com.hotpads.joblet.JobletCodec;

public class JobletType<P>{
	private final String persistentString;
	private final Supplier<JobletCodec<P>> codecSupplier;
	private final Class<? extends Joblet<P>> clazz;
	private final Integer batchSize;
	private final Integer cpuPermits;
	private final Integer memoryPermits;
	private final boolean causesScaling;

	public JobletType(String persistentString, Supplier<JobletCodec<P>> codecSupplier,
			Class<? extends Joblet<P>> clazz, Integer batchSize, Integer cpuPermits,
			Integer memoryPermits, boolean causesScaling){
		this.persistentString = persistentString;
		this.codecSupplier = codecSupplier;
		this.clazz = clazz;
		this.batchSize = batchSize;
		this.cpuPermits = cpuPermits;
		this.memoryPermits = memoryPermits;
		this.causesScaling = causesScaling;
	}

	public String getDisplay(){
		return getPersistentString();
	}

	/*-------------- get -------------------*/

	public String getPersistentString(){
		return persistentString;
	}

	public Supplier<? extends JobletCodec<P>> getCodecSupplier(){
		return codecSupplier;
	}

	public Class<? extends Joblet<P>> getAssociatedClass(){
		return clazz;
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
