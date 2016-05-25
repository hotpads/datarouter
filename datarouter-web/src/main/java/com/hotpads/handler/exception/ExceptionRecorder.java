package com.hotpads.handler.exception;

import com.hotpads.datarouter.exception.ExceptionCategory;

public interface ExceptionRecorder{

	ExceptionRecord tryRecordException(Throwable exception, String fallbackLocation);
	ExceptionRecord tryRecordException(Throwable exception, String fallbackLocation, ExceptionCategory category);
	ExceptionRecord recordException(Throwable exception, ExceptionCategory category, String location);
}
