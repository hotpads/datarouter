package com.hotpads.handler.user.authenticate.authenticator.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByApiKeyLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.util.core.exception.InvalidApiCallException;

public class DatarouterApiKeyAuthenticator extends BaseDatarouterAuthenticator {
	
	private DatarouterAuthenticationConfig authenticationConfig;
	private DatarouterUserNodes userNodes;
	
	public DatarouterApiKeyAuthenticator(HttpServletRequest request, HttpServletResponse response,
			DatarouterAuthenticationConfig authenticationConfig, DatarouterUserNodes userNodes) {
		super(request, response);
		this.authenticationConfig = authenticationConfig;
		this.userNodes = userNodes;
	}
	
	@Override
	public DatarouterSession getSession() {
		if(!request.getServletPath().startsWith(authenticationConfig.getApiPath())) {
			return null;
		}
		String apiKey = request.getParameter(authenticationConfig.getApiKeyParam());
		DatarouterUser user = lookupUserByApiKey(apiKey);
		DatarouterSession session = DatarouterSession.createFromUser(user);
		session.setIncludeSessionCookie(false);
		return session;
	}
	
	private DatarouterUser lookupUserByApiKey(String apiKey) {
		if (DrStringTool.isNullOrEmpty(apiKey)) {
			throw new InvalidApiCallException("no api key specified");
		}

		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByApiKeyLookup(apiKey), null);

		if (user == null) {
			throw new InvalidApiCallException("no user found with provided api key");
		}
		if (DrBooleanTool.isFalseOrNull(user.getEnabled())) {
			throw new InvalidApiCallException("user is not enabled");
		}
		if (DrBooleanTool.isFalseOrNull(user.getApiEnabled())) {
			throw new InvalidApiCallException("user does not have api authorization");
		}

		return user;
	}
}
