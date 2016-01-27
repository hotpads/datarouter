package com.hotpads.handler.user.authenticate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.authenticator.DatarouterAuthenticator;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.role.DatarouterUserRole;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.user.session.DatarouterSessionManager;
import com.hotpads.util.core.exception.InvalidApiCallException;
import com.hotpads.util.core.exception.InvalidCredentialsException;
import com.hotpads.util.core.io.RuntimeIOException;
import com.hotpads.util.http.RequestTool;
import com.hotpads.util.http.ResponseTool;

@Singleton
public class DatarouterAuthenticationFilter implements Filter{
	private static Logger logger = LoggerFactory.getLogger(DatarouterAuthenticationFilter.class);
	
	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private DatarouterUserNodes userNodes;
	@Inject
	private DatarouterSessionManager sessionManager;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException{
	}
	
	@Override
	public void destroy(){
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException,
			ServletException{
		final HttpServletRequest request = (HttpServletRequest)req;
		final HttpServletResponse response = (HttpServletResponse)res;
		
		final String contextPath = request.getContextPath();
		final String signinFormPath = authenticationConfig.getSigninPath();
		final String signinSubmitPath = authenticationConfig.getSigninSubmitPath();
		final String path = request.getServletPath();
//		final String uri = request.getRequestURI();// for debugging
		final String urlWithQueryString = getUrlWithQueryString(request);
		final URL targetUrl = getValidTargetUrl(request, signinFormPath);
		final URL referrerUrl = getReferrerUrl(request);

		
		//special case where they clicked sign-in from a random page and we want to bounce them back to that page
		if(shouldBounceBack(request, path, signinFormPath, referrerUrl, targetUrl)){
			sessionManager.addTargetUrlCookie(response, referrerUrl.toExternalForm());
		}

		
		//obtain a valid datarouterSession or redirect to the login form
		DatarouterSession datarouterSession;
		try{
			datarouterSession = getSessionAndAddToRequest(request, response);
		}catch(InvalidCredentialsException e){//authenticators should throw this exception for bad credentials
			logger.warn(e.getMessage());
			handleBadCredentials(request, response, contextPath, signinFormPath);
			return;
		} catch(InvalidApiCallException e) {
			logger.warn(e.getMessage());
			handleBadApiCall(response, e.getMessage());
			return;
		}

		//we identified the user, now check they have the needed roles for the uri
		if(missingRequiredRoles(path, datarouterSession)){
			handleMissingRoles(request, response, urlWithQueryString, contextPath, signinFormPath, datarouterSession);
			return;
		}

		// successful login.  redirect 
		if(DrObjectTool.equals(path, signinSubmitPath)){
			handleSuccessfulLogin(request, response, targetUrl);
			return;
		}

		filterChain.doFilter(req, res);
	}
	
	
	/****************** private methods **************************/
	
	private static String getUrlWithQueryString(HttpServletRequest request){
		String queryString = (request.getQueryString() != null) ? "?" + request.getQueryString() : "";
		return request.getRequestURL().toString() + queryString;
	}
	
	private static URL getReferrerUrl(HttpServletRequest request){
		final String referrerString = request.getHeader("referer"); // misspelled on purpose
		if(DrStringTool.isEmpty(referrerString)){ 
			return null; 
		}
		try{
			return new URL(referrerString);
		}catch(MalformedURLException e){
			throw new IllegalArgumentException("invalid referer:"+referrerString);
		}
	}
	
	private URL getValidTargetUrl(HttpServletRequest request, String signinFormPath){
		URL targetUrl = sessionManager.getTargetUrlFromCookie(request);
		if(targetUrl==null){ 
			return null; 
		}
		if(DrObjectTool.equals(signinFormPath, targetUrl.getPath())){
			logger.warn("ignoring targetUrl "+targetUrl.getPath());
			return null; 
		}
		return targetUrl;
	}
	
	//case where they clicked sign-in from a random page on the site, and we want to send them back to that page
	private static boolean shouldBounceBack(HttpServletRequest request, String path, String signinFormPath, 
			URL referrer, URL targetUrl){
		boolean referredFromThisHost = referrer != null
				&& DrObjectTool.equals(referrer.getHost(), request.getServerName());
		boolean noExplicitTargetUrl = targetUrl == null;
		boolean displayingLoginForm = DrObjectTool.equals(signinFormPath, path);
		return referredFromThisHost && noExplicitTargetUrl && displayingLoginForm;
	}

	private DatarouterSession getSessionAndAddToRequest(HttpServletRequest request, HttpServletResponse response){
		Iterable<DatarouterAuthenticator> authenticators = authenticationConfig.getAuthenticators(request,
				response);
		for(DatarouterAuthenticator authenticator : authenticators){
			DatarouterSession session = authenticator.getSession();
			if(session != null){
				sessionManager.addToRequest(request, session);
				if(DrBooleanTool.isTrue(session.getPersistent())) {
					sessionManager.addUserTokenCookie(response, session.getUserToken());
					sessionManager.addSessionTokenCookie(response, session.getSessionToken());
					userNodes.getSessionNode().put(session, null);
				}
				return session;
			}
		}
		throw new RuntimeException("no session returned.  make sure you have a catch-all authenticator");
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
		String usernameParamAndValue = DrStringTool.isEmpty(attemptedUsername) ? "" : "&" + usernameParam + "="
				+ escapedUsername;
		String errorParam = "?error=true" + usernameParamAndValue;
		ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, contextPath + signinFormPath 
				+ errorParam);
	}
	
	private void handleBadApiCall(HttpServletResponse response, String message){
		ResponseTool.sendErrorInJson(response, HttpServletResponse.SC_BAD_REQUEST, message);
	}

	private boolean missingRequiredRoles(String path, DatarouterSession datarouterSession){
		Collection<DatarouterUserRole> requiredRoles = authenticationConfig.getRequiredRoles(path);
		boolean userHasAllRoles = datarouterSession.getRoles().containsAll(requiredRoles);
		return ! userHasAllRoles;
	}
	
	private void handleMissingRoles(HttpServletRequest request, HttpServletResponse response, String url,
			String contextPath, String signinFormPath, DatarouterSession datarouterSession){
		if(datarouterSession.isAnonymous()){// trump the referrer url
			sessionManager.addTargetUrlCookie(response, url);
			ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, contextPath 
					+ signinFormPath);
		}else{
			try{
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}catch(IOException e){
				throw new RuntimeIOException(e);
			}
		}
	}
	
	private void handleSuccessfulLogin(HttpServletRequest request, HttpServletResponse response, URL targetUrl){
		String redirectTo;
		if(targetUrl != null){
			redirectTo = targetUrl.toExternalForm();
			sessionManager.clearTargetUrlCookie(response);
		}else{
			redirectTo = request.getContextPath();
		}
		ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, redirectTo);
	}
}
