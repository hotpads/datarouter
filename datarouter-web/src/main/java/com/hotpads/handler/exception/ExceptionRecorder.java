package com.hotpads.handler.exception;

public interface ExceptionRecorder{

	ExceptionRecord tryRecordException(Exception exception, String fallbackLocation);

}
