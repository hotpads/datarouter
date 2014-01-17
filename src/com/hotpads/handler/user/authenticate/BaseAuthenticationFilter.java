package com.hotpads.handler.user.authenticate;

package com.hotpads.websupport.authentication;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.hotpads.handler.ResponseTool;
import com.hotpads.handler.user.session.DatarouterSessionKeys;
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
	//why are these static?
	protected static String loginFormURI = "";
	protected static String loginSubmitURI = "";

	protected ServletContext servletContext;

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
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		HttpSession session = request.getSession();
		loginFormURI = request.getContextPath() + URL_LOGIN;
		loginSubmitURI = request.getContextPath() + URL_LOGIN_SUBMIT;

		// parse inputs
		final String uri = request.getRequestURI();// for debugging
		final String queryString = (request.getQueryString() != null) ? "?" + request.getQueryString() : "";
		final String url = request.getRequestURL().toString() + queryString;
		final String interceptedUrlString = (String)session.getAttribute(DatarouterSessionKeys.targetUrl.toString());
		URL interceptedUrl = null;
		try{
			interceptedUrl = new URL(interceptedUrlString);
		}catch(MalformedURLException e){
		}
		final String referrer = request.getHeader("referer"); // misspelled on purpose

		// store the referrer so we can bounce the user back to the page they clicked "sign in" from
		if((loginFormURI.equals(uri)) && interceptedUrl == null // only if they clicked "sign-in" manually
				&& StringTool.notEmpty(referrer)){
			try{
				URL referringUrl = new URL(referrer);
				if(referringUrl.getHost().equals(request.getServerName())){
					session.setAttribute(DatarouterSessionKeys.targetUrl.toString(), referrer);
				}
			}catch(MalformedURLException e){
			}
		}

		// figure out who they are
		RequestAuthentication userSession = null;
		try{
			userSession = getUserSession(request, response);
			if(userSession == null){// NewUserAuthenticator didn't return a userSession
				throw new NullPointerException("no user session");
			}
		}catch(InvalidCredentialsException e){
			logger.warn(e.getMessage());
			String attemptedUsername = RequestTool.get(request, PARAM_USERNAME, "");
			String escapedUsername = URLEncoder.encode(attemptedUsername, "UTF-8");
			String usernameParam = StringTool.isEmpty(attemptedUsername) ? "" : "&" + PARAM_USERNAME + "="
					+ escapedUsername;
			String errorParam = "?error=true" + usernameParam;
			ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, loginFormURI + errorParam);
			return;
		}

		// check their UserRoles
		if(missingRequiredRoles(userSession)){
			if(userSession.isAnonymous()){// trump the referrer url
				session.setAttribute(DatarouterSessionKeys.targetUrl.toString(), url);
				SessionDao.saveAcegiSecurityTargetUrl(UserTool.getSessionToken(request), request.getRequestURL()
						.toString());
				ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, loginFormURI);
			}else{
				response.sendError(403);// 403=permission denied
			}
			return;
		}

		// successful login
		if(loginSubmitURI.equals(uri)){
			handleSuccessfulLogin(request, response, interceptedUrl, interceptedUrlString);
			return;
		}

		filterChain.doFilter(req, res);
	}

	
	protected void handleSuccessfulLogin(HttpServletRequest request, HttpServletResponse response, 
			URL interceptedUrl, String interceptedUrlString){
		//decide where to redirect to
		if(interceptedUrl != null 
				&& ObjectTool.notEquals(loginFormURI, interceptedUrl.getPath())) { 
			//if already logged in will always redirect to localhost
			ResponseTool.sendRedirect(response, HttpServletResponse.SC_SEE_OTHER, interceptedUrl.toExternalForm());
		}else{
			ResponseTool.sendRedirect(request, response, HttpServletResponse.SC_SEE_OTHER, request.getContextPath());
		}
	}


	protected RequestAuthentication getUserSession(HttpServletRequest request, HttpServletResponse response){
		for(BaseDatarouterAuthenticator authenticator : IterableTool.nullSafe(getAuthenticators(request, response))){
			RequestAuthentication authentication = authenticator.authenticate();
			if(authentication != null){
				RequestAuthentication.cacheInRequest(request, authentication);
				break;
			}
		}
		return null;
	}

	private boolean missingRequiredRoles(RequestAuthentication authentication){
		Collection<DatarouterUserRole> requiredRoles = getRequiredRoles();
		return ! requiredRoles.containsAll(authentication.getRoles());
	}
}
