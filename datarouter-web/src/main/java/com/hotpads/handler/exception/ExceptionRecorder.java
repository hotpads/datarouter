package com.hotpads.handler.exception;

public interface ExceptionRecorder{

	ExceptionRecord tryRecordException(Exception exception, String fallbackLocation);
	ExceptionRecord tryRecordException(Exception exception, String fallbackLocation, ExceptionReporter reporter);
	ExceptionRecord recordException(Exception exception, ExceptionReporter reporter, String location);
}
