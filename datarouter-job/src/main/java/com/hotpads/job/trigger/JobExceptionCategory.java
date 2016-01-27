package com.hotpads.job.trigger;

import com.hotpads.datarouter.exception.ExceptionCategory;

public enum JobExceptionCategory implements ExceptionCategory{

	JOB,
	JOBLET //TODO move to JobletExceptionCategory after moving joblets to a datarouter module

}
