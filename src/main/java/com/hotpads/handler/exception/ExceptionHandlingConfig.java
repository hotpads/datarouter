package com.hotpads.handler.exception;

import javax.servlet.http.HttpServletRequest;

public interface ExceptionHandlingConfig {

	boolean shouldDisplayStackTrace(HttpServletRequest request, Exception exception);

	boolean shouldRepportError(HttpServletRequest request, Exception exception);

	boolean shouldLogException(HttpServletRequest request, Exception exception);

	String getHtmlErrorMessage(Exception exception);

	String getServerName();
	
	String getRecipientEmail();

	String getNotificationApiEndPoint();

}
