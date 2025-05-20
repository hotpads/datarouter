/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.web.user.authenticate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.authenticate.authenticator.DatarouterAuthenticator;
import io.datarouter.auth.authenticate.authenticator.DatarouterAuthenticator.DatarouterSessionAndPersist;
import io.datarouter.auth.config.DatarouterAuthenticationConfig;
import io.datarouter.auth.exception.InvalidCredentialsException;
import io.datarouter.auth.session.DatarouterSessionManager;
import io.datarouter.auth.storage.user.session.BaseDatarouterSessionDao;
import io.datarouter.auth.storage.user.session.DatarouterSession;
import io.datarouter.util.BooleanTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.WebAppLifecycle;
import io.datarouter.web.WebAppLifecycleState;
import io.datarouter.web.exception.InvalidApiCallException;
import io.datarouter.web.shutdown.ShutdownService;
import io.datarouter.web.util.http.RequestTool;
import io.datarouter.web.util.http.ResponseTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterAuthenticationFilter implements Filter{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterAuthenticationFilter.class);

	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private BaseDatarouterSessionDao datarouterSessionDao;
	@Inject
	private DatarouterSessionManager sessionManager;
	@Inject
	private ShutdownService shutdownService;
	@Inject
	private WebAppLifecycle webAppLifeCycle;

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
	throws IOException, ServletException{
		final HttpServletRequest request = (HttpServletRequest)req;
		final HttpServletResponse response = (HttpServletResponse)res;

		shutdownService.logIfLate(request);
		webAppLifeCycle.set(WebAppLifecycleState.HTTP_READY);

		final String contextPath = request.getContextPath();
		final String signinFormPath = authenticationConfig.getSigninPath();
		final String signinSubmitPath = authenticationConfig.getSigninSubmitPath();
		final String path = request.getServletPath();
		final URL targetUrl = getValidTargetUrl(request, signinFormPath);
		final URL referrerUrl = getReferrerUrl(request);

		//special case where they clicked sign-in from a random page and we want to bounce them back to that page
		if(shouldBounceBack(request, path, signinFormPath, referrerUrl, targetUrl)){
			sessionManager.addTargetUrlCookie(response, referrerUrl.toExternalForm());
		}

		//obtain a valid datarouterSession or redirect to the login form
		try{
			addSessionToRequest(request, response);
		}catch(InvalidCredentialsException e){//authenticators should throw this exception for bad credentials
			logger.warn(e.getMessage());
			handleBadCredentials(request, response, contextPath, signinFormPath);
			return;
		}catch(InvalidApiCallException e){
			logger.warn(e.getMessage());
			handleBadApiCall(response, e.getMessage());
			return;
		}

		// successful login.  redirect
		if(Objects.equals(path, signinSubmitPath)){
			handleSuccessfulLogin(request, response, targetUrl);
			return;
		}

		filterChain.doFilter(req, res);
	}

	/*------------------------------ private --------------------------------*/

	private static URL getReferrerUrl(HttpServletRequest request){
		final String referrerString = request.getHeader("referer"); // misspelled on purpose
		if(StringTool.isEmpty(referrerString)){
			return null;
		}
		try{
			return new URL(referrerString);
		}catch(MalformedURLException e){
			// some referrers (like android-app://com.google.android.gm) are not valid URL: don't fail, just log
			logger.info("invalid referer: {}", referrerString);
			return null;
		}
	}

	private URL getValidTargetUrl(HttpServletRequest request, String signinFormPath){
		URL targetUrl = sessionManager.getTargetUrlFromCookie(request);
		if(targetUrl == null){
			return null;
		}
		if(Objects.equals(signinFormPath, targetUrl.getPath())){
			logger.warn("ignoring targetUrl {}", targetUrl.getPath());
			return null;
		}
		return targetUrl;
	}

	//case where they clicked sign-in from a random page on the site, and we want to send them back to that page
	private static boolean shouldBounceBack(HttpServletRequest request, String path, String signinFormPath,
			URL referrer, URL targetUrl){
		boolean referredFromThisHost = referrer != null
				&& Objects.equals(referrer.getHost(), request.getServerName());
		boolean noExplicitTargetUrl = targetUrl == null;
		boolean displayingLoginForm = Objects.equals(signinFormPath, path);
		return referredFromThisHost && noExplicitTargetUrl && displayingLoginForm;
	}

	private void addSessionToRequest(HttpServletRequest request, HttpServletResponse response){
		List<DatarouterAuthenticator> authenticators = authenticationConfig.getAuthenticators(request);
		for(DatarouterAuthenticator authenticator : authenticators){
			DatarouterSessionAndPersist sessionAndPersist = authenticator.getSession(request, response);
			DatarouterSession session = sessionAndPersist.session();
			if(session != null){
				DatarouterSessionManager.addToRequest(request, session);
				if(BooleanTool.isTrue(sessionAndPersist.persist())){
					sessionManager.addUserTokenCookie(response, session.getUserToken());
					sessionManager.addSessionTokenCookie(response, session.getSessionToken());
					datarouterSessionDao.put(session);
				}
				return;
			}
		}
		throw new RuntimeException("no session returned, make sure you have a catch-all authenticator");
	}

	private void handleBadCredentials(HttpServletRequest request, HttpServletResponse response, String contextPath,
			String signinFormPath){
		String usernameParam = authenticationConfig.getUsernameParam();
		String attemptedUsername = RequestTool.get(request, usernameParam, "");
		String escapedUsername;
		try{
			escapedUsername = URLEncoder.encode(attemptedUsername, "UTF-8");
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException(e);
		}
		String usernameParamAndValue = StringTool.isEmpty(attemptedUsername) ? "" : "&" + usernameParam + "="
				+ escapedUsername;
		String errorParam = "?error=true" + usernameParamAndValue;
		ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, contextPath + signinFormPath
				+ errorParam);
	}

	private void handleBadApiCall(HttpServletResponse response, String message) throws IOException{
		ResponseTool.sendJsonForMessage(response, HttpServletResponse.SC_BAD_REQUEST, message);
	}

	private void handleSuccessfulLogin(HttpServletRequest request, HttpServletResponse response, URL targetUrl){
		String redirectTo = request.getContextPath();
		if(targetUrl != null){
			if(!targetUrl.getPath().equals(request.getContextPath() + authenticationConfig.getSigninPath())){
				redirectTo = targetUrl.toExternalForm();
			}
			sessionManager.clearTargetUrlCookie(response);
		}
		ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, redirectTo);
	}

}
