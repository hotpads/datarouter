package com.hotpads.config.job.enums;

import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.job.joblet.JobletProcess;

public interface JobletDef<T> extends StringEnum<T>{

	Class<? extends JobletProcess> getAssociatedClass();
	boolean getRateLimited();
	Integer getBatchSize();
	Integer getCpuPermits();
	Integer getMemoryPermits();

}
