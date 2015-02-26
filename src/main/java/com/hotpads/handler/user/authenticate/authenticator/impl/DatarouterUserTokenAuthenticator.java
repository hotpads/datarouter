package com.hotpads.handler.user.authenticate.authenticator.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.user.session.DatarouterSessionManager;

public class DatarouterUserTokenAuthenticator extends BaseDatarouterAuthenticator{
	
	private DatarouterSessionManager sessionManager;
	
	public DatarouterUserTokenAuthenticator(HttpServletRequest request, HttpServletResponse response,
			DatarouterSessionManager sessionManager) {
		super(request, response);
		this.sessionManager = sessionManager;
	}

	@Override
	public DatarouterSession getSession(){
		String userToken = sessionManager.getUserTokenFromCookie(request);
		if(DrStringTool.isEmpty(userToken)){
			return null;
		}
		
		//user has been to the site before.  create a new session with their previous userToken
		DatarouterSession session = DatarouterSession.createAnonymousSession(userToken);
		return session;
	}
	
	
}
