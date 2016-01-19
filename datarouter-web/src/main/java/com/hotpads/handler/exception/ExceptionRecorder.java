package com.hotpads.handler.exception;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrExceptionTool;
import com.hotpads.notification.ParallelApiCaller;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.databean.NotificationUserType;
import com.hotpads.util.http.security.UrlScheme;

public class ExceptionRecorder{
	private static final Logger logger = LoggerFactory.getLogger(ExceptionRecorder.class);

	@Inject
	private ExceptionNodes exceptionNodes;
	@Inject
	private ParallelApiCaller notificationApiCaller;
	@Inject
	private ExceptionHandlingConfig exceptionHandlingConfig;

	public ExceptionRecord tryRecordException(Exception exception, String fallbackLocation){
		return tryRecordException(exception, fallbackLocation, ExceptionReporter.UNKNOWN);
	}

	public ExceptionRecord tryRecordException(Exception exception, String fallbackLocation,	ExceptionReporter reporter){
		try{
			String location;
			StackTraceElement[] stackTrace = exception.getStackTrace();
			if(stackTrace.length > 0){
				StackTraceElement stackTraceElement = stackTrace[0];
				location = stackTraceElement.getClassName() + ":" + stackTraceElement.getLineNumber();
			}else{
				location = fallbackLocation;
			}
			return recordException(exception, reporter, location);
		}catch(Exception e){
			logger.warn("Exception while recording an exception", e);
		}
		return null;
	}

	public ExceptionRecord recordException(Exception exception, ExceptionReporter reporter, String location){
		ExceptionCounters.inc(reporter.name());
		ExceptionCounters.inc(exception.getClass().getName());
		ExceptionCounters.inc(location);
		ExceptionCounters.inc(exception.getClass().getName() + " " + location);
		ExceptionRecord exceptionRecord = new ExceptionRecord(
				exceptionHandlingConfig.getServerName(),
				DrExceptionTool.getStackTraceAsString(exception),
				exception.getClass().getName());
		exceptionNodes.getExceptionRecordNode().put(exceptionRecord, null);
		String domain = exceptionHandlingConfig.isDevServer() ? UrlScheme.LOCAL_DEV_SERVER_HTTPS
				: UrlScheme.DOMAIN_NAME;
		logger.warn("Exception recorded (https://" + domain + "/analytics/exception/details?exceptionRecord="
				+ exceptionRecord.getKey().getId() + ")");
		if(exceptionHandlingConfig.shouldReportError(exception)){
			NotificationRequest notificationRequest = new NotificationRequest(
					new NotificationUserId(
							NotificationUserType.EMAIL,
							exceptionHandlingConfig.getRecipientEmail()),
					reporter.get(exceptionHandlingConfig),
					exceptionRecord.getKey().getId(),
					location);// This is the email subject
			notificationApiCaller.add(notificationRequest, exceptionRecord);
		}
		return exceptionRecord;
	}

}
