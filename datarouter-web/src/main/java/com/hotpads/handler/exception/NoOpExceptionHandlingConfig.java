package com.hotpads.handler.exception;

import javax.servlet.http.HttpServletRequest;

import com.hotpads.exception.analysis.ExceptionDto;

public class NoOpExceptionHandlingConfig implements ExceptionHandlingConfig{

	@Override
	public boolean shouldDisplayStackTrace(HttpServletRequest request, Exception exception){
		return false;
	}

	@Override
	public boolean shouldReportError(Throwable exception){
		return false;
	}

	@Override
	public boolean shouldReportError(ExceptionDto dto){
		return false;
	}

	@Override
	public String getHtmlErrorMessage(Exception exception){
		return null;
	}

	@Override
	public String getServerName(){
		return null;
	}

	@Override
	public boolean isDevServer(){
		return false;
	}

	@Override
	public String getRecipientEmail(){
		return null;
	}

	@Override
	public String getNotificationApiEndPoint(){
		return null;
	}

}
