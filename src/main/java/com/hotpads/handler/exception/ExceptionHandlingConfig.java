package com.hotpads.handler.exception;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

public interface ExceptionHandlingConfig {

	boolean shouldDisplayStackTrace(HttpServletRequest request, Exception exception);

	boolean shouldRepportError(HttpServletRequest request, Exception exception);

	boolean shouldLogException(HttpServletRequest request, Exception exception);

	String getErrorPage(Exception exception) throws IOException;

}
