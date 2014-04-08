package com.hotpads.handler.user.authenticate.authenticator.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.session.DatarouterSession;

public class DatarouterApiKeyAuthenticator extends BaseDatarouterAuthenticator {
	
	public DatarouterApiKeyAuthenticator(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}
	
	@Override
	public DatarouterSession getSession() {
		return null; //TODO
	}
}
