package com.hotpads.handler.user.authenticate.authenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.user.authenticate.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.user.session.DatarouterSessionTool;

public class DatarouterNewUserAuthenticator extends BaseDatarouterAuthenticator{

	public DatarouterNewUserAuthenticator(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}
	
	@Override
	public DatarouterSession getSession(){
		DatarouterSession session = DatarouterSession.createNewAnonymousSession();
		DatarouterSessionTool.addUserTokenCookie(response, session.getUserToken());
		DatarouterSessionTool.addSessionTokenCookie(response, session.getSessionToken());
		return session;
	}
}
