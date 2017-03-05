package com.hotpads.handler.exception;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.hotpads.datarouter.monitoring.exception.ExceptionDto;
import com.hotpads.handler.user.role.DatarouterUserRole;
import com.hotpads.handler.user.session.DatarouterSessionManager;

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
	public boolean shouldReportError(ExceptionRecord exceptionRecord){
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

}
