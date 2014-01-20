package com.hotpads.handler.user.authenticate.authenticator.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.user.session.DatarouterSessionTool;
import com.hotpads.util.core.StringTool;

public class DatarouterUserTokenAuthenticator extends BaseDatarouterAuthenticator{
	
	public DatarouterUserTokenAuthenticator(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public DatarouterSession getSession(){
		String userToken = DatarouterSessionTool.getUserTokenFromCookie(request);
		if(StringTool.isEmpty(userToken)){
			return null;
		}
		
		//user has been to the site before.  create a new session with their previous userToken
		DatarouterSession session = DatarouterSession.createAnonymousSession(userToken);
		return session;
	}
	
	
}
