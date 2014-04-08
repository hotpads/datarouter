package com.hotpads.handler.user.authenticate.authenticator.impl;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByApiKeyLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.exception.InvalidCredentialsException;

public class DatarouterApiKeyAuthenticator extends BaseDatarouterAuthenticator {
	
	DatarouterUserNodes userNodes;
	
	public DatarouterApiKeyAuthenticator(HttpServletRequest request, HttpServletResponse response,
			DatarouterUserNodes userNodes) {
		super(request, response);
		this.userNodes = userNodes;
	}
	
	@Override
	public DatarouterSession getSession() {
		String apiKey = request.getParameter("apiKey");
		
		DatarouterUser user = lookupUserByApiKey(apiKey);
		
		user.setLastLoggedIn(new Date());
		userNodes.getUserNode().put(user, null);
		
		DatarouterSession session = DatarouterSession.createFromUser(user);
		return session;
	}
	
	private DatarouterUser lookupUserByApiKey(String apiKey) {
		if (StringTool.isNullOrEmpty(apiKey)) {
			throw new InvalidCredentialsException("no api key specified");
		}

		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByApiKeyLookup(apiKey), null);

		if (user == null) {
			throw new InvalidCredentialsException("no user found with provided api key");
		}
		if (BooleanTool.isFalseOrNull(user.isEnabled())) {
			throw new InvalidCredentialsException("user is not enabled");
		}
		if (BooleanTool.isFalseOrNull(user.isApiEnabled())) {
			throw new InvalidCredentialsException("user does not have api authorization");
		}

		return user;
	}
}
