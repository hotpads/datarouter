package com.hotpads.handler.user.authenticate.authenticator.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByApiKeyLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.exception.BadApiCallException;

public class DatarouterApiKeyAuthenticator extends BaseDatarouterAuthenticator {
	
	private DatarouterUserNodes userNodes;
	private DatarouterAuthenticationConfig authenticationConfig;
	
	public DatarouterApiKeyAuthenticator(HttpServletRequest request, HttpServletResponse response,
			DatarouterAuthenticationConfig authenticationConfig, DatarouterUserNodes userNodes) {
		super(request, response);
		this.userNodes = userNodes;
		this.authenticationConfig = authenticationConfig;
	}
	
	@Override
	public DatarouterSession getSession() {
		if(!request.getServletPath().startsWith(authenticationConfig.getApiPath())) {
			return null;
		}
		String apiKey = request.getParameter("apiKey");
		DatarouterUser user = lookupUserByApiKey(apiKey);
		DatarouterSession session = DatarouterSession.createFromUser(user);
		session.setSessionPersistent(false);
		return session;
	}
	
	private DatarouterUser lookupUserByApiKey(String apiKey) {
		if (StringTool.isNullOrEmpty(apiKey)) {
			throw new BadApiCallException("no api key specified");
		}

		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByApiKeyLookup(apiKey), null);

		if (user == null) {
			throw new BadApiCallException("no user found with provided api key");
		}
		if (BooleanTool.isFalseOrNull(user.isEnabled())) {
			throw new BadApiCallException("user is not enabled");
		}
		if (BooleanTool.isFalseOrNull(user.isApiEnabled())) {
			throw new BadApiCallException("user does not have api authorization");
		}

		return user;
	}
}
