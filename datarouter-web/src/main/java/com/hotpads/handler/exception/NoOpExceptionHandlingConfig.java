package com.hotpads.handler.exception;

import javax.servlet.http.HttpServletRequest;

import com.hotpads.notification.type.NotificationType;

public class NoOpExceptionHandlingConfig implements ExceptionHandlingConfig{

	@Override
	public boolean shouldDisplayStackTrace(HttpServletRequest request, Exception exception){
		return false;
	}

	@Override
	public boolean shouldReportError(Exception exception){
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

	@Override
	public Class<? extends NotificationType> getDefaultErrorNotificationType(){
		return null;
	}

	@Override
	public Class<? extends NotificationType> getServerErrorNotificationType(){
		return null;
	}

	@Override
	public Class<? extends NotificationType> getJobErrorNotificationType(){
		return null;
	}

	@Override
	public Class<? extends NotificationType> getJobletErrorNotificationType(){
		return null;
	}

}
