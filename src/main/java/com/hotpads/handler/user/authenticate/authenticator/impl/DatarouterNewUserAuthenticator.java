package com.hotpads.handler.user.authenticate.authenticator.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.user.authenticate.DatarouterTokenGenerator;
import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.user.session.DatarouterSessionManager;

public class DatarouterNewUserAuthenticator extends BaseDatarouterAuthenticator{
	
	private DatarouterSessionManager sessionManager;
	
	public DatarouterNewUserAuthenticator(HttpServletRequest request, HttpServletResponse response,
			DatarouterSessionManager sessionManager) {
		super(request, response);
		this.sessionManager = sessionManager;
	}
	
	@Override
	public DatarouterSession getSession(){
		String userToken = DatarouterTokenGenerator.generateRandomToken();
		DatarouterSession session = DatarouterSession.createAnonymousSession(userToken);
		sessionManager.addUserTokenCookie(response, session.getUserToken());
		sessionManager.addSessionTokenCookie(response, session.getSessionToken());
		return session;
	}
}
