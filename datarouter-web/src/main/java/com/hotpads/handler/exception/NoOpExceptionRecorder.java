package com.hotpads.handler.exception;

import com.hotpads.datarouter.exception.ExceptionCategory;

public class NoOpExceptionRecorder implements ExceptionRecorder{

	@Override
	public ExceptionRecord tryRecordException(Throwable exception, String fallbackLocation){
		return null;
	}

	@Override
	public ExceptionRecord tryRecordException(Throwable exception, String fallbackLocation, ExceptionCategory category){
		return null;
	}

	@Override
	public ExceptionRecord recordException(Throwable exception, ExceptionCategory category, String location){
		return null;
	}

}
