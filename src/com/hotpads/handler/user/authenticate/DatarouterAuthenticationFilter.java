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

import org.apache.log4j.Logger;

import com.hotpads.handler.ResponseTool;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.authenticator.DatarouterAuthenticator;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.role.DatarouterUserRole;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.user.session.DatarouterSessionTool;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.exception.InvalidCredentialsException;
import com.hotpads.util.core.io.RuntimeIOException;

@Singleton
public class DatarouterAuthenticationFilter implements Filter{
	private static Logger logger = Logger.getLogger(DatarouterAuthenticationFilter.class);
	
	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;
	@Inject
	private DatarouterUserNodes userNodes;

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
		
		final String loginFormPath = authenticationConfig.getSigninFormPath();
		final String loginSubmitPath = authenticationConfig.getSigninSubmitPath();
		final String path = request.getServletPath();
//		final String uri = request.getRequestURI();// for debugging
		final String urlWithQueryString = getUrlWithQueryString(request);
		final URL targetUrl = getValidTargetUrl(request, loginFormPath);
		final URL referrerUrl = getReferrerUrl(request);

		
		//special case where they clicked sign-in from a random page and we want to bounce them back to that page
		if(shouldBounceBack(request, path, loginFormPath, referrerUrl, targetUrl)){
			DatarouterSessionTool.addTargetUrlCookie(response, referrerUrl.toExternalForm());
		}

		
		//obtain a valid datarouterSession or redirect to the login form
		DatarouterSession datarouterSession;
		try{
			datarouterSession = getAndCacheSession(request, response);
		}catch(InvalidCredentialsException e){//authenticators should throw this exception for bad credentials
			logger.warn(e.getMessage());
			handleBadCredentials(request, response, loginFormPath);
			return;
		}

		//we identified the user, now check they have the needed roles for the uri
		if(missingRequiredRoles(path, datarouterSession)){
			handleMissingRoles(request, response, urlWithQueryString, loginFormPath, datarouterSession);
			return;
		}

		// successful login.  redirect 
		if(ObjectTool.equals(path, loginSubmitPath)){
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
		if(StringTool.isEmpty(referrerString)){ return null; }
		try{
			return new URL(referrerString);
		}catch(MalformedURLException e){
			throw new IllegalArgumentException("invalid referer:"+referrerString);
		}
	}
	
	private static URL getValidTargetUrl(HttpServletRequest request, String loginFormPath){
		URL targetUrl = DatarouterSessionTool.getTargetUrlFromCookie(request);
		if(targetUrl==null){ return null; }
		if(ObjectTool.equals(loginFormPath, targetUrl.getPath())){
			logger.warn("ignoring targetUrl "+targetUrl.getPath());
			return null; 
		}
		return targetUrl;
	}
	
	//case where they clicked sign-in from a random page on the site, and we want to send them back to that page
	private static boolean shouldBounceBack(HttpServletRequest request, String path, String loginFormPath, URL referrer, 
			URL targetUrl){
		boolean referredFromThisHost = referrer != null
				&& ObjectTool.equals(referrer.getHost(), request.getServerName());
		boolean noExplicitTargetUrl = targetUrl == null;
		boolean displayingLoginForm = ObjectTool.equals(loginFormPath, path);
		return referredFromThisHost && noExplicitTargetUrl && displayingLoginForm;
	}

	private DatarouterSession getAndCacheSession(HttpServletRequest request, HttpServletResponse response){
		Iterable<DatarouterAuthenticator> authenticators = authenticationConfig.getAuthenticators(request,
				response);
		for(DatarouterAuthenticator authenticator : authenticators){
			DatarouterSession session = authenticator.getSession();
			if(session != null){
				DatarouterSessionTool.addToRequest(request, session);
				userNodes.getSessionNode().put(session, null);
				return session;
			}
		}
		throw new RuntimeException("no session returned.  make sure you have a catch-all authenticator");
	}

	private void handleBadCredentials(HttpServletRequest request, HttpServletResponse response, String loginFormPath){
		String usernameParamName = authenticationConfig.getUsernameParamName();
		String attemptedUsername = RequestTool.get(request, usernameParamName, "");
		String escapedUsername;
		try{
			escapedUsername = URLEncoder.encode(attemptedUsername, "UTF-8");
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException(e);
		}
		String usernameParam = StringTool.isEmpty(attemptedUsername) ? "" : "&" + usernameParamName + "="
				+ escapedUsername;
		String errorParam = "?error=true" + usernameParam;
		ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, loginFormPath + errorParam);
	}

	private boolean missingRequiredRoles(String path, DatarouterSession datarouterSession){
		Collection<DatarouterUserRole> requiredRoles = authenticationConfig.getRequiredRoles(path);
		return ! requiredRoles.containsAll(datarouterSession.getRoles());
	}
	
	private static void handleMissingRoles(HttpServletRequest request, HttpServletResponse response, String url,
			String loginFormPath, DatarouterSession datarouterSession){
		if(datarouterSession.isAnonymous()){// trump the referrer url
			DatarouterSessionTool.addTargetUrlCookie(response, url);
			ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, loginFormPath);
		}else{
			try{
				response.sendError(403);//403=permission denied
			}catch(IOException e){
				throw new RuntimeIOException(e);
			}
		}
	}
	
	private static void handleSuccessfulLogin(HttpServletRequest request, HttpServletResponse response, URL targetUrl){
		String redirectTo;
		if(targetUrl != null){
			redirectTo = targetUrl.toExternalForm();
			DatarouterSessionTool.clearTargetUrlCookie(response);
		}else{
			redirectTo = request.getContextPath();
		}
		ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, redirectTo);
	}
}
