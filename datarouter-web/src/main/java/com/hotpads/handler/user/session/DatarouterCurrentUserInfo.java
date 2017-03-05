package com.hotpads.handler.user.session;

import java.util.Optional;

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

	@Override
	public Optional<String> getUserToken(HttpServletRequest request){
		return sessionManager.getFromRequest(request)
				.map(DatarouterSession::getUserToken);
	}

}
