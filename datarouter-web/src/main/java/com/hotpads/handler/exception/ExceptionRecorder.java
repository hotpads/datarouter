package com.hotpads.handler.exception;

import com.hotpads.datarouter.exception.ExceptionCategory;

public interface ExceptionRecorder{

	ExceptionRecord tryRecordException(Exception exception, String fallbackLocation);
	ExceptionRecord tryRecordException(Exception exception, String fallbackLocation, ExceptionCategory category);
	ExceptionRecord recordException(Exception exception, ExceptionCategory category, String location);
}
