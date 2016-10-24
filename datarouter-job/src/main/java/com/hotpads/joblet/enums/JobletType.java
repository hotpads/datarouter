package com.hotpads.joblet.enums;

import java.util.function.Supplier;

import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.joblet.Joblet;
import com.hotpads.joblet.JobletCodec;

public class JobletType<P> implements Comparable<JobletType<?>>{
	private final int persistentInt;
	private final String persistentString;
	private final Supplier<JobletCodec<P>> codecSupplier;
	private final Class<? extends Joblet<P>> clazz;
	private final Integer cpuPermits;
	private final Integer memoryPermits;
	private final boolean causesScaling;

	public JobletType(int persistentInt, String persistentString, Supplier<JobletCodec<P>> codecSupplier,
			Class<? extends Joblet<P>> clazz, Integer cpuPermits, Integer memoryPermits, boolean causesScaling){
		this.persistentInt = persistentInt;
		this.persistentString = persistentString;
		this.codecSupplier = codecSupplier;
		this.clazz = clazz;
		this.cpuPermits = cpuPermits;
		this.memoryPermits = memoryPermits;
		this.causesScaling = causesScaling;
	}

	public String getDisplay(){
		return getPersistentString();
	}

	@Override
	public String toString(){
		return getPersistentString();
	}

	/*-------------- Comparable ---------------*/

	@Override
	public int compareTo(JobletType<?> other){
		return DrComparableTool.nullFirstCompareTo(this.persistentString, other.persistentString);
	}

	/*-------------- get -------------------*/

	public String getPersistentString(){
		return persistentString;
	}

	public int getPersistentInt(){
		return persistentInt;
	}

	public Supplier<? extends JobletCodec<P>> getCodecSupplier(){
		return codecSupplier;
	}

	public Class<? extends Joblet<P>> getAssociatedClass(){
		return clazz;
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
