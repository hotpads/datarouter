package com.hotpads.handler.exception;

import javax.servlet.http.HttpServletRequest;

import com.hotpads.notification.type.NotificationType;

public interface ExceptionHandlingConfig {

	boolean shouldDisplayStackTrace(HttpServletRequest request, Exception exception);
	boolean shouldReportError(Exception exception);

	String getHtmlErrorMessage(Exception exception);
	String getServerName();
	boolean isDevServer();
	String getRecipientEmail();
	String getNotificationApiEndPoint();
	Class<? extends NotificationType> getDefaultErrorNotificationType();
	Class<? extends NotificationType> getServerErrorNotificationType();
	Class<? extends NotificationType> getJobErrorNotificationType();
	Class<? extends NotificationType> getJobletErrorNotificationType();

}
