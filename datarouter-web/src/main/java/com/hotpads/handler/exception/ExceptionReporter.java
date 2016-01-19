package com.hotpads.handler.exception;

import java.util.function.Function;

import com.hotpads.notification.type.NotificationType;

public enum ExceptionReporter{

	UNKNOWN(ExceptionHandlingConfig::getDefaultErrorNotificationType),
	HTTP_REQUEST(ExceptionHandlingConfig::getServerErrorNotificationType),
	JOB(ExceptionHandlingConfig::getJobErrorNotificationType),
	JOBLET(ExceptionHandlingConfig::getJobletErrorNotificationType),
	;

	private final Function<ExceptionHandlingConfig,Class<? extends NotificationType>> typeGetter;

	private ExceptionReporter(Function<ExceptionHandlingConfig,Class<? extends NotificationType>> typeGetter){
		this.typeGetter = typeGetter;
	}

	public Class<? extends NotificationType> get(ExceptionHandlingConfig exceptionHandlingConfig){
		return typeGetter.apply(exceptionHandlingConfig);
	}

}
