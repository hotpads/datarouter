package com.hotpads.handler.user.authenticate.authenticator.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.datarouter.client.imp.http.ApacheHttpClient;
import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByApiKeyLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.util.core.exception.InvalidApiCallException;

public class DatarouterSignatureAuthenticator extends BaseDatarouterAuthenticator{
	
	private DatarouterAuthenticationConfig authenticationConfig;
	private DatarouterUserNodes userNodes;
	
	public DatarouterSignatureAuthenticator(HttpServletRequest request, HttpServletResponse response,
			DatarouterAuthenticationConfig authenticationConfig, DatarouterUserNodes userNodes){
		super(request, response);
		this.authenticationConfig = authenticationConfig;
		this.userNodes = userNodes;
	}

	@Override
	public DatarouterSession getSession(){
		if(!request.getServletPath().startsWith(authenticationConfig.getApiPath())) {
			return null;
		}
		DatarouterSession session = getSession(request);
	
		return session;
	}
	
	private DatarouterSession getSession(HttpServletRequest request) {
		String apiKey = request.getParameter(authenticationConfig.getApiKeyParam());
		String signature = request.getParameter(authenticationConfig.getSignatureParam());
		DatarouterUser user = lookupUserByApiKeyAndValidate(apiKey);
		
		String uri = request.getRequestURI();
		Map<String, String> params = RequestTool.getMapOfParameters(request);
		params.remove("signature");
		String expectedSignature = ApacheHttpClient.generateSignature(uri, params, user.getSecretKey());		
		if(DrObjectTool.notEquals(expectedSignature, signature)){
			throw new InvalidApiCallException("invalid signature specified");
		}
		DatarouterSession session = DatarouterSession.createFromUser(user);
		session.setIncludeSessionCookie(false);
		
		return session;
	}

	
	//copy from DatarouterApiKeyAuthenticator
	private DatarouterUser lookupUserByApiKeyAndValidate(String apiKey) {
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
