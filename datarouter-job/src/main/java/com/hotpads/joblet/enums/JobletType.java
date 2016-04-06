package com.hotpads.joblet.enums;

import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.joblet.Joblet;

public interface JobletType<T> extends StringEnum<T>{

	Class<? extends Joblet<?>> getAssociatedClass();
	boolean getRateLimited();
	Integer getBatchSize();
	Integer getCpuPermits();
	Integer getMemoryPermits();
	boolean causesScaling();

	String getDisplay();

}
