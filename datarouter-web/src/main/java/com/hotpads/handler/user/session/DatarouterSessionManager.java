package com.hotpads.handler.user.session;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.util.http.CookieTool;

@Singleton
public class DatarouterSessionManager{

	@Inject
	private DatarouterAuthenticationConfig config;

	/************* static fields **********************/

	private final static String REQUEST_ATTRIBUTE_NAME = "datarouterSession";
	private static final int
		TARGET_URL_COOKIE_EXPIRATION_SECONDS = 30 * 60,
		USER_TOKEN_COOKIE_EXPIRATION_SECONDS = 365 * 24 * 3600;


	/************* targetUrl *********************/

	public void addTargetUrlCookie(HttpServletResponse response, String targetUrl){
		CookieTool.addCookie(response, config.getTargetUrlName(), targetUrl, "/",
				TARGET_URL_COOKIE_EXPIRATION_SECONDS);
	}

	public URL getTargetUrlFromCookie(HttpServletRequest request){
		String targetUrlString = CookieTool.getCookieValue(request, config.getTargetUrlName());
		if(DrStringTool.isEmpty(targetUrlString)){ return null; }
		try{
			return new URL(targetUrlString);
		}catch(MalformedURLException e){
			throw new IllegalArgumentException("invalid targetUrl:"+targetUrlString);
		}
	}

	public void clearTargetUrlCookie(HttpServletResponse response){
		CookieTool.deleteCookie(response, config.getTargetUrlName());
	}


	/************* sessionToken *********************/

	public void addSessionTokenCookie(HttpServletResponse response, String sessionToken){
		CookieTool.addCookie(response, config.getSessionTokenCookieName(), sessionToken, "/",
				config.getSessionTokenTimeoutSeconds());
	}

	public String getSessionTokenFromCookie(HttpServletRequest request){
		return CookieTool.getCookieValue(request, config.getSessionTokenCookieName());
	}

	public void clearSessionTokenCookie(HttpServletResponse response){
		CookieTool.deleteCookie(response, config.getSessionTokenCookieName());
	}


	/************* userToken *********************/

	public void addUserTokenCookie(HttpServletResponse response, String userToken){
		CookieTool.addCookie(response, config.getUserTokenCookieName(), userToken, "/",
				USER_TOKEN_COOKIE_EXPIRATION_SECONDS);
	}

	public String getUserTokenFromCookie(HttpServletRequest request){
		return CookieTool.getCookieValue(request, config.getUserTokenCookieName());
	}

	public void clearUserTokenCookie(HttpServletResponse response){
		CookieTool.deleteCookie(response, config.getUserTokenCookieName());
	}


	/************ add/remove session from request *********/

	public void addToRequest(HttpServletRequest request, DatarouterSession userSession) {
		request.setAttribute(REQUEST_ATTRIBUTE_NAME, userSession);
	}

	public Optional<DatarouterSession> getFromRequest(HttpServletRequest request) {
		return Optional.ofNullable((DatarouterSession)request.getAttribute(REQUEST_ATTRIBUTE_NAME));
	}

}
