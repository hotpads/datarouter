package com.hotpads.handler.exception;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.hotpads.exception.analysis.ExceptionDto;
import com.hotpads.handler.user.role.DatarouterUserRole;
import com.hotpads.handler.user.session.DatarouterSessionManager;
import com.hotpads.notification.type.NotificationType;

@Singleton
public class NoOpExceptionHandlingConfig implements ExceptionHandlingConfig{

	@Inject
	private DatarouterSessionManager sessionManager;

	@Override
	public boolean shouldDisplayStackTrace(HttpServletRequest request, Exception exception){
		return sessionManager
			.getFromRequest(request)
			.map(DatarouterUserRole::isSessionAdmin)
			.orElse(false);
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
		return "Error";
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
