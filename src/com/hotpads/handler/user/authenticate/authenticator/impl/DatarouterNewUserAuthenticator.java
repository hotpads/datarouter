package com.hotpads.handler.user.authenticate.authenticator.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.user.authenticate.DatarouterTokenGenerator;
import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.user.session.DatarouterSessionManager;

public class DatarouterNewUserAuthenticator extends BaseDatarouterAuthenticator{

	public DatarouterNewUserAuthenticator(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}
	
	@Override
	public DatarouterSession getSession(){
		String userToken = DatarouterTokenGenerator.generateRandomToken();
		DatarouterSession session = DatarouterSession.createAnonymousSession(userToken);
		DatarouterSessionManager.addUserTokenCookie(response, session.getUserToken());
		DatarouterSessionManager.addSessionTokenCookie(response, session.getSessionToken());
		return session;
	}
}
