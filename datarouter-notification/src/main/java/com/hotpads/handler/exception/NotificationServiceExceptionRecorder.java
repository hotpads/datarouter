package com.hotpads.handler.exception;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.exception.ExceptionCategory;
import com.hotpads.datarouter.exception.UnknownExceptionCategory;
import com.hotpads.datarouter.util.core.DrExceptionTool;
import com.hotpads.notification.ParallelApiCaller;
import com.hotpads.notification.databean.NotificationRequest;
import com.hotpads.notification.databean.NotificationUserId;
import com.hotpads.notification.databean.NotificationUserType;
import com.hotpads.util.http.security.UrlScheme;

public class NotificationServiceExceptionRecorder implements ExceptionRecorder{
	private static final Logger logger = LoggerFactory.getLogger(ExceptionRecorder.class);

	@Inject
	private ExceptionNodes exceptionNodes;
	@Inject
	private ParallelApiCaller notificationApiCaller;
	@Inject
	private ExceptionHandlingConfig exceptionHandlingConfig;
	@Inject
	private ExceptionCategoryNotificationTypeFactory exceptionCategoryNotificationTypeFactory;


	@Override
	public ExceptionRecord tryRecordException(Throwable exception, String fallbackLocation){
		return tryRecordException(exception, fallbackLocation, UnknownExceptionCategory.UNKNOWN);
	}


	@Override
	public ExceptionRecord tryRecordException(Throwable exception, String fallbackLocation,
			ExceptionCategory exceptionCategory){
		try{
			String location;
			StackTraceElement[] stackTrace = exception.getStackTrace();
			if(stackTrace.length > 0){
				StackTraceElement stackTraceElement = stackTrace[0];
				location = stackTraceElement.getClassName() + ":" + stackTraceElement.getLineNumber();
			}else{
				location = fallbackLocation;
			}
			return recordException(exception, exceptionCategory, location);
		}catch(Exception e){
			logger.warn("Exception while recording an exception", e);
		}
		return null;
	}


	@Override
	public ExceptionRecord recordException(Throwable exception,
			ExceptionCategory exceptionCategory, String location){
		ExceptionCounters.inc(exceptionCategory.name());
		ExceptionCounters.inc(exception.getClass().getName());
		ExceptionCounters.inc(location);
		ExceptionCounters.inc(exception.getClass().getName() + " " + location);
		ExceptionRecord exceptionRecord = new ExceptionRecord(exceptionHandlingConfig.getServerName(), DrExceptionTool
				.getStackTraceAsString(exception), exception.getClass().getName(), location);
		exceptionNodes.getExceptionRecordNode().put(exceptionRecord, null);
		String domain = exceptionHandlingConfig.isDevServer() ? UrlScheme.LOCAL_DEV_SERVER_HTTPS
				: UrlScheme.DOMAIN_NAME;
		logger.warn("Exception recorded (https://" + domain + "/analytics/exception/details?exceptionRecord="
				+ exceptionRecord.getKey().getId() + ")");
		if(exceptionHandlingConfig.shouldReportError(exception)){
			NotificationUserId notificationUserId = new NotificationUserId(
					NotificationUserType.EMAIL,
					exceptionHandlingConfig.getRecipientEmail());
			NotificationRequest notificationRequest = new NotificationRequest(
					notificationUserId,
					exceptionCategoryNotificationTypeFactory.getNotificationType(exceptionCategory),
					exceptionRecord.getKey().getId(),
					location);// This is the email subject
			notificationApiCaller.add(notificationRequest, exceptionRecord);
		}
		return exceptionRecord;
	}

}
