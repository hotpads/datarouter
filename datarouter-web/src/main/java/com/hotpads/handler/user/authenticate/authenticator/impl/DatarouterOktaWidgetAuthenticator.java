package com.hotpads.handler.user.authenticate.authenticator.impl;

import java.util.Collections;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByUsernameLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.authenticate.okta.OktaSessionResponse;
import com.hotpads.handler.user.authenticate.okta.OktaSettings;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.util.core.exception.InvalidCredentialsException;
import com.hotpads.util.http.RequestTool;
import com.hotpads.util.http.client.HotPadsHttpClient;
import com.hotpads.util.http.client.HotPadsHttpClientBuilder;
import com.hotpads.util.http.request.HotPadsHttpRequest;
import com.hotpads.util.http.request.HotPadsHttpRequest.HttpRequestMethod;
import com.hotpads.util.http.response.exception.HotPadsHttpRuntimeException;

public class DatarouterOktaWidgetAuthenticator extends BaseDatarouterAuthenticator{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterOktaWidgetAuthenticator.class);

	private static final String SESSIONS_PATH = "/api/v1/sessions/";
	private static final String API_KEY_PREFIX = "SSWS ";
	private static final String P_OKTA_SESSION_ID = "okta-session-id";
	private static final String P_OKTA_LOGIN = "okta-login";

	private final DatarouterAuthenticationConfig authenticationConfig;
	private final DatarouterUserNodes userNodes;
	private final OktaSettings oktaSettings;
	private final HotPadsHttpClient httpClient;

	public DatarouterOktaWidgetAuthenticator(HttpServletRequest request, HttpServletResponse response,
			DatarouterAuthenticationConfig authenticationConfig, DatarouterUserNodes userNodes,
			OktaSettings oktaSettings){
		super(request, response);
		this.authenticationConfig = authenticationConfig;
		this.userNodes = userNodes;
		this.oktaSettings = oktaSettings;
		this.httpClient = new HotPadsHttpClientBuilder().build();
	}

	@Override
	public DatarouterSession getSession(){
		if(DrObjectTool.notEquals(request.getServletPath(), authenticationConfig.getSigninSubmitPath())
				|| !oktaSettings.getShouldProcess()){
			//this isn't the login form or Okta is not enabled
			return null;
		}

		String oktaSessionId = RequestTool.get(request, P_OKTA_SESSION_ID, "");
		String oktaLogin = RequestTool.get(request, P_OKTA_LOGIN, "");
		if(DrStringTool.isEmpty(oktaSessionId) || DrStringTool.isEmpty(oktaSessionId)){
			return null;
		}

		DatarouterUser user = getUserByOktaSession(oktaSessionId, oktaLogin);
		user.setLastLoggedIn(new Date());
		userNodes.getUserNode().put(user, null);
		return DatarouterSession.createFromUser(user);
	}

	private DatarouterUser getUserByOktaSession(String oktaSessionId, String oktaLogin){
		HotPadsHttpRequest oktaRequest = new HotPadsHttpRequest(HttpRequestMethod.GET, oktaSettings.orgUrl
				.getValue() + SESSIONS_PATH + oktaSessionId, false);
		oktaRequest.addHeaders(Collections.singletonMap("Authorization", API_KEY_PREFIX + oktaSettings.apiKey
				.getValue()));

		try{
			OktaSessionResponse oktaResponse = httpClient.execute(oktaRequest, OktaSessionResponse.class);
			logger.debug("Okta response: login: {}, status: {}, isActive: {}", oktaResponse.login, oktaResponse.status,
					oktaResponse.isActive());
			return lookupAndValidateUser(oktaLogin, oktaResponse);
		}catch(HotPadsHttpRuntimeException e){
			logger.error("Failed to authenticate with Okta", e);
			throw new InvalidCredentialsException("Failed to authenticate with Okta");
		}
	}

	private DatarouterUser lookupAndValidateUser(String username, OktaSessionResponse oktaResponse){
		if(!oktaResponse.isActive()){
			throw new InvalidCredentialsException("Provided session is not active.");
		}
		//prevent people from using someone else's session ID to gain access to own account
		if(!username.equals(oktaResponse.login)){
			throw new InvalidCredentialsException("Provided login does not match Okta session login.");
		}

		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByUsernameLookup(username), null);

		if(user == null){
			throw new InvalidCredentialsException("user not found (" + username + ")");
		}
		if(DrBooleanTool.isFalseOrNull(user.getEnabled())){
			throw new InvalidCredentialsException("user not enabled (" + username + ")");
		}
		return user;
	}
}
