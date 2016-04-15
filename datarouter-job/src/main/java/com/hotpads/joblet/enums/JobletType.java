package com.hotpads.joblet.enums;

import com.hotpads.joblet.Joblet;

public interface JobletType<P>{

	String getPersistentString();
	Class<? extends Joblet<P>> getAssociatedClass();
	boolean getRateLimited();
	Integer getBatchSize();
	Integer getCpuPermits();
	Integer getMemoryPermits();
	boolean causesScaling();

	default String getDisplay(){
		return getPersistentString();
	}

}
