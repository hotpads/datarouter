package com.hotpads.joblet.type;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.joblet.Joblet;
import com.hotpads.joblet.JobletCodec;
import com.hotpads.joblet.JobletConstants;

public class JobletType<P> implements Comparable<JobletType<?>>{

	private final int persistentInt;
	private final String persistentString;
	private final String shortQueueName;//must be short for some queueing systems
	private final Supplier<JobletCodec<P>> codecSupplier;
	private final Class<? extends Joblet<P>> clazz;
	private final Integer cpuPermits;
	private final Integer memoryPermits;
	private final boolean causesScaling;

	public JobletType(int persistentInt, String persistentString, String shortQueueName,
			Supplier<JobletCodec<P>> codecSupplier, Class<? extends Joblet<P>> clazz, Integer cpuPermits,
			Integer memoryPermits, boolean causesScaling){
		this.persistentInt = persistentInt;
		this.persistentString = persistentString;
		Preconditions.checkArgument(shortQueueName.length() <= JobletConstants.MAX_LENGTH_SHORT_QUEUE_NAME);
		this.shortQueueName =  shortQueueName;
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
		return DrComparableTool.nullFirstCompareTo(persistentString, other.persistentString);
	}

	/*--------------- static -------------------*/

	public static void assertAllSameShortQueueName(Collection<JobletType<?>> jobletTypes){
		JobletType<?> first = DrCollectionTool.getFirst(jobletTypes);
		for(JobletType<?> jobletType : jobletTypes){
			Preconditions.checkState(Objects.equals(first.getShortQueueName(), jobletType.getShortQueueName()));
		}
	}

	/*-------------- get -------------------*/

	public String getPersistentString(){
		return persistentString;
	}

	public String getShortQueueName(){
		return shortQueueName;
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
