package com.hotpads.handler.user.session;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

@Singleton
public class DatarouterCurrentUserInfo implements CurrentUserInfo{

	@Inject
	private DatarouterSessionManager sessionManager;

	@Override
	public String getEmail(HttpServletRequest request){
		DatarouterSession session = sessionManager.getFromRequest(request).get();
		return session.getUsername();
	}

}
