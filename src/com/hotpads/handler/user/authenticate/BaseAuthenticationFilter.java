package com.hotpads.handler.user.authenticate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.hotpads.handler.CookieTool;
import com.hotpads.handler.DatarouterCookieKeys;
import com.hotpads.handler.ResponseTool;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.user.session.DatarouterSessionDao;
import com.hotpads.handler.user.session.DatarouterSessionKeys;
import com.hotpads.handler.user.session.DatarouterSessionTool;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.exception.InvalidCredentialsException;

public abstract class BaseAuthenticationFilter implements Filter{
	protected static Logger logger = Logger.getLogger(BaseAuthenticationFilter.class);
	protected static final String PARAM_USERNAME = "loginUsername";
	protected static final String PARAM_PASSWORD = "loginPassword";
	protected static final String URL_LOGIN = "/login";
	protected static final String URL_LOGIN_SUBMIT = "/login/submit";

	protected ServletContext servletContext;
	
	@Inject
	private DatarouterSessionDao datarouterSessionDao;

	protected abstract Collection<DatarouterUserRole> getRequiredRoles();

	protected abstract Iterable<BaseDatarouterAuthenticator> getAuthenticators(HttpServletRequest request,
			HttpServletResponse response);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException{
		servletContext = filterConfig.getServletContext();
	}
	
	@Override
	public void destroy(){
	}


	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException,
			ServletException{
		
		/*********** parse inputs *****************/
		
		final HttpServletRequest request = (HttpServletRequest)req;
		final HttpServletResponse response = (HttpServletResponse)res;
		
		//these should always be the same, but easiest to get them each request
		final String loginFormUri = request.getContextPath() + URL_LOGIN;
		final String loginSubmitUri = request.getContextPath() + URL_LOGIN_SUBMIT;

		// parse inputs
		final String uri = request.getRequestURI();// for debugging
		final String queryString = (request.getQueryString() != null) ? "?" + request.getQueryString() : "";
		final String url = request.getRequestURL().toString() + queryString;
		final URL targetUrl = DatarouterSessionTool.getTargetUrlFromCookie(request);
		final URL referrer = getReferrerUrl(request);
		final String sessionToken = DatarouterSessionTool.getSessionTokenFromCookie(request);

		
		// store the referrer so we can bounce the user back to the page they clicked "sign in" from
		if((loginFormUri.equals(uri)) 
				&& targetUrl == null // only if they clicked "sign-in" manually
				&& referrer != null){
			if(referrer.getHost().equals(request.getServerName())){//why this?
				DatarouterSessionTool.addTargetUrlCookie(response, referrer.toExternalForm());
			}
		}

		
		//obtain a valid datarouterSession or redirect to the login form
		DatarouterSession datarouterSession;
		try{
			datarouterSession = Preconditions.checkNotNull(getAndCacheSession(request, response));
		}catch(InvalidCredentialsException e){//authenticators should throw this exception for bad credentials
			logger.warn(e.getMessage());
			handleInvalidLogin(request, response, loginFormUri);
			return;
		}

		//we identified the user, now check they have the needed roles for the uri
		if(missingRequiredRoles(datarouterSession)){
			if(datarouterSession.doesUserHaveRole(DatarouterUserRole.anonymous)){// trump the referrer url
				DatarouterSessionTool.addTargetUrlCookie(response, url);
				ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, loginFormUri);
			}else{
				response.sendError(403);// 403=permission denied
			}
			return;
		}

		// successful login
		if(loginSubmitUri.equals(uri)){
			handleSuccessfulLogin(request, response, loginFormUri, interceptedUrl, interceptedUrlString);
			return;
		}

		filterChain.doFilter(req, res);
	}
	
	
	/****************** private methods **************************/
	
	private static URL getReferrerUrl(HttpServletRequest request){
		final String referrerString = request.getHeader("referer"); // misspelled on purpose
		if(StringTool.isEmpty(referrerString)){ return null; }
		try{
			return new URL(referrerString);
		}catch(MalformedURLException e){
			throw new IllegalArgumentException("invalid referer:"+referrerString);
		}
	}

	private void handleInvalidLogin(HttpServletRequest request, HttpServletResponse response, String loginFormUri){
		String attemptedUsername = RequestTool.get(request, PARAM_USERNAME, "");
		String escapedUsername;
		try{
			escapedUsername = URLEncoder.encode(attemptedUsername, "UTF-8");
		}catch(UnsupportedEncodingException e){
			throw new RuntimeException(e);
		}
		String usernameParam = StringTool.isEmpty(attemptedUsername) ? "" : "&" + PARAM_USERNAME + "="
				+ escapedUsername;
		String errorParam = "?error=true" + usernameParam;
		ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, loginFormUri + errorParam);
	}
	
	private void handleSuccessfulLogin(HttpServletRequest request, HttpServletResponse response, 
			String loginFormUri, URL interceptedUrl, String interceptedUrlString){
		//decide where to redirect to
		if(interceptedUrl != null 
				&& ObjectTool.notEquals(loginFormUri, interceptedUrl.getPath())) { 
			//if already logged in will always redirect to localhost
			ResponseTool.sendRedirect(response, HttpServletResponse.SC_SEE_OTHER, interceptedUrl.toExternalForm());
		}else{
			ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, request.getContextPath());
		}
	}

	private DatarouterSession getAndCacheSession(HttpServletRequest request, HttpServletResponse response){
		for(BaseDatarouterAuthenticator authenticator : IterableTool.nullSafe(getAuthenticators(request, response))){
			DatarouterSession authentication = authenticator.getSession();
			if(authentication != null){
				DatarouterSessionTool.addToRequest(request, authentication);
				break;
			}
		}
		return null;
	}

	private boolean missingRequiredRoles(DatarouterSession datarouterSession){
		Collection<DatarouterUserRole> requiredRoles = getRequiredRoles();
		return ! requiredRoles.containsAll(datarouterSession.getRoles());
	}
}
