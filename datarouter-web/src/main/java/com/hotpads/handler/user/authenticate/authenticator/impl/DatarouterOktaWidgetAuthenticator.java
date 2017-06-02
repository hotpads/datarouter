package com.hotpads.handler.user.authenticate.authenticator.impl;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.hotpads.handler.user.authenticate.okta.OktaUserResponse;
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

	private static final Entry<String, String> CACHE_HEADER = new AbstractMap.SimpleEntry<>(
			"Cache", "no-cache, no-store");
	private static final Entry<String, String> CONTENT_TYPE_HEADER = new AbstractMap.SimpleEntry<>(
			"Content-Type", "application/json");
	private static final Entry<String, String> ACCEPT_HEADER = new AbstractMap.SimpleEntry<>(
			"Accept", "application/json");

	private static final String SESSIONS_PATH = "/api/v1/sessions/";
	private static final String USERS_PATH = "/api/v1/users/";
	private static final String AUTH_HEADER = "Authorization";
	private static final String API_KEY_PREFIX = "SSWS ";

	private static final String P_OKTA_SESSION_ID = "okta-session-id";
	private static final String P_OKTA_LOGIN = "okta-login";

	private static final Map<String, String> COMMON_HEADERS = Collections.unmodifiableMap(Stream
			.of(CACHE_HEADER,CONTENT_TYPE_HEADER, ACCEPT_HEADER)
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

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
		addOktaHeaders(oktaRequest);
		debugRequest(oktaRequest);

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

	private void addOktaHeaders(HotPadsHttpRequest request){
		Map<String, String> headers = new HashMap<>(COMMON_HEADERS);
		headers.put(AUTH_HEADER, API_KEY_PREFIX + oktaSettings.apiKey.getValue());
		request.addHeaders(headers);
	}

	private void debugRequest(HotPadsHttpRequest oktaRequest){
		Map<String, String> headers = new HashMap<>(oktaRequest.getHeaders());
		for(String key : headers.keySet()){
			if(AUTH_HEADER.equals(key)){
				String value = headers.get(key);
				String censored = value.substring(0, API_KEY_PREFIX.length() + 3) + "***" + value.substring(value
						.length() - 3);
				headers.put(key, censored);
			}
		}
		StringBuilder debugMessage = new StringBuilder()
				.append("\nmethod: ").append(oktaRequest.getMethod())
				.append("\nurl: ").append(oktaRequest.getUrl())
				.append("\nheaders: ").append(headers.toString())
				.append("\nget params: ").append(oktaRequest.getGetParams().toString())
				.append("\nbody params: ").append(oktaRequest.getPostParams().toString())
				.append("\nentity: ").append(oktaRequest.canHaveEntity() ? oktaRequest.getEntityAsString() : "none");

		logger.debug(debugMessage.toString());
	}

	private DatarouterUser lookupAndValidateUser(String username, OktaSessionResponse oktaResponse){
		//prevent people from using someone else's session ID to gain access to own account
		if(!username.equals(oktaResponse.login)){
			throw new InvalidCredentialsException("Provided login does not match Okta session login.");
		}

		if(!oktaResponse.isActive()){
			//This is BS from Okta, either a very badly designed API or a bug (not sure yet). The session is active.
			//The session's createdAt can be checked against user's lastLogin to verify that it is an active session.
			if(oktaResponse.isMfaRequired()){
				validateActiveSessionWorkaround(oktaResponse);
			}else{
				throw new InvalidCredentialsException("Provided session is not active.");
			}
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

	private void validateActiveSessionWorkaround(OktaSessionResponse sessionResponse){
		if(sessionResponse.createdAt == null || sessionResponse.expiresAt == null){
			throw new InvalidCredentialsException("Okta session missing dates");
		}
		Date now = new Date();
		if(now.before(sessionResponse.createdAt) || now.after(sessionResponse.expiresAt)){
			throw new InvalidCredentialsException("Okta session has impossible dates");
		}

		HotPadsHttpRequest oktaRequest = new HotPadsHttpRequest(HttpRequestMethod.GET, oktaSettings.orgUrl
				.getValue() + USERS_PATH + sessionResponse.userId, false);
		addOktaHeaders(oktaRequest);
		debugRequest(oktaRequest);
		OktaUserResponse userResponse = httpClient.execute(oktaRequest, OktaUserResponse.class);
		if(!userResponse.isActive()){
			throw new InvalidCredentialsException("Okta session has inactive user");
		}
		if(userResponse.lastLogin == null || !sessionResponse.createdAt.equals(userResponse.lastLogin)){
			throw new InvalidCredentialsException("Okta session creation and user login mismatch");
		}
	}
}
