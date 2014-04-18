package com.hotpads.handler.user.authenticate.authenticator.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.dao.DatarouterUserDao;
import com.hotpads.handler.user.session.DatarouterSession;

public class DatarouterApiKeyAuthenticator extends BaseDatarouterAuthenticator {
	
	private DatarouterUserDao userDao;
	private DatarouterAuthenticationConfig authenticationConfig;
	
	public DatarouterApiKeyAuthenticator(HttpServletRequest request, HttpServletResponse response,
			DatarouterAuthenticationConfig authenticationConfig, DatarouterUserNodes userNodes) {
		super(request, response);
		userDao = new DatarouterUserDao();
		this.authenticationConfig = authenticationConfig;
	}
	
	@Override
	public DatarouterSession getSession() {
		if(!request.getServletPath().startsWith(authenticationConfig.getApiPath())) {
			return null;
		}
		String apiKey = request.getParameter(authenticationConfig.getApiKeyParam());
		DatarouterUser user = userDao.lookupUserByApiKey(apiKey);
		DatarouterSession session = DatarouterSession.createFromUser(user);
		session.setIncludeSessionCookie(false);
		return session;
	}
}
