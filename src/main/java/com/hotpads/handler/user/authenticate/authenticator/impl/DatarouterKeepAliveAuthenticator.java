package com.hotpads.handler.user.authenticate.authenticator.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.session.DatarouterSession;

public class DatarouterKeepAliveAuthenticator extends BaseDatarouterAuthenticator {

	private DatarouterAuthenticationConfig authenticationConfig;
	
	public DatarouterKeepAliveAuthenticator(HttpServletRequest request, HttpServletResponse response,
			DatarouterAuthenticationConfig authenticationConfig) {
		super(request, response);
		this.authenticationConfig = authenticationConfig;
	}

	@Override
	public DatarouterSession getSession() {
		if(!request.getRequestURI().endsWith(authenticationConfig.getKeepAlivePath())) {
			return null;
		}
		DatarouterSession session = DatarouterSession.createAnonymousSession(null);
		session.setPersistent(false);
		return session;
	}

}
