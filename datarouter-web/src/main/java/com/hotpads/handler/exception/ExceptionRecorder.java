package com.hotpads.handler.exception;

public interface ExceptionRecorder<C extends ExceptionCategory>{

	ExceptionRecord tryRecordException(Exception exception, String fallbackLocation);
	ExceptionRecord tryRecordException(Exception exception, String fallbackLocation, C category);
	ExceptionRecord recordException(Exception exception, C category, String location);
}
