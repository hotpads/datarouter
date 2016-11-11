package com.hotpads.handler.exception;

import javax.servlet.http.HttpServletRequest;

import com.hotpads.exception.analysis.ExceptionDto;

public interface ExceptionHandlingConfig{

	boolean shouldDisplayStackTrace(HttpServletRequest request, Exception exception);
	boolean shouldReportError(Throwable exception);
	boolean shouldReportError(ExceptionDto dto);

	String getHtmlErrorMessage(Exception exception);
	String getServerName();
	boolean isDevServer();
	String getRecipientEmail();
	String getNotificationApiEndPoint();

}
