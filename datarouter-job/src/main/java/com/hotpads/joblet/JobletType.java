package com.hotpads.joblet;

import com.hotpads.datarouter.storage.field.enums.StringEnum;

public interface JobletType<T> extends StringEnum<T>{

	Class<? extends JobletProcess> getAssociatedClass();
	boolean getRateLimited();
	Integer getBatchSize();
	Integer getCpuPermits();
	Integer getMemoryPermits();
	boolean causesScaling();

	String getDisplay();

}
