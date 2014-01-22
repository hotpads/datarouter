package com.hotpads.handler.user.session;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.CookieTool;
import com.hotpads.handler.DatarouterCookieKeys;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.util.core.StringTool;

public class DatarouterSessionManager{
	
	@Inject
	private DatarouterAuthenticationConfig config;
	
	/************* static fields **********************/

	private final static String REQUEST_ATTRIBUTE_NAME = "userSession";
	private static final int 
		TARGET_URL_COOKIE_EXPIRATION_SECONDS = 30 * 60,
		USER_TOKEN_COOKIE_EXPIRATION_SECONDS = 365 * 24 * 3600;
		
	
	/************* targetUrl *********************/
	
	public static void addTargetUrlCookie(HttpServletResponse response, String targetUrl){
		CookieTool.addCookie(response, DatarouterCookieKeys.targetUrl.toString(), targetUrl, "/", 
				TARGET_URL_COOKIE_EXPIRATION_SECONDS);
	}
	
	public static URL getTargetUrlFromCookie(HttpServletRequest request){
		String targetUrlString = CookieTool.getCookieValue(request, DatarouterCookieKeys.targetUrl.toString());
		if(StringTool.isEmpty(targetUrlString)){ return null; }
		try{
			return new URL(targetUrlString);
		}catch(MalformedURLException e){
			throw new IllegalArgumentException("invalid targetUrl:"+targetUrlString);
		}
	}
	
	public static void clearTargetUrlCookie(HttpServletResponse response){
		CookieTool.deleteCookie(response, DatarouterCookieKeys.targetUrl.toString());
	}
	
	
	/************* sessionToken *********************/
	
	public void addSessionTokenCookie(HttpServletResponse response, String sessionToken){
		CookieTool.addCookie(response, config.getSessionTokenCookieName(), sessionToken, "/", 
				config.getSessionTokenTimeoutSeconds());
	}
	
	public static String getSessionTokenFromCookie(HttpServletRequest request){
		return CookieTool.getCookieValue(request, DatarouterCookieKeys.sessionToken.toString());
	}
	
	public static void clearSessionTokenCookie(HttpServletResponse response){
		CookieTool.deleteCookie(response, DatarouterCookieKeys.sessionToken.toString());
	}

	
	/************* userToken *********************/
	
	public void addUserTokenCookie(HttpServletResponse response, String userToken){
		CookieTool.addCookie(response, config.getUserTokenCookieName(), userToken, "/", 
				USER_TOKEN_COOKIE_EXPIRATION_SECONDS);
	}
	
	public static String getUserTokenFromCookie(HttpServletRequest request){
		return CookieTool.getCookieValue(request, DatarouterCookieKeys.userToken.toString());
	}
	
	public static void clearUserTokenCookie(HttpServletResponse response){
		CookieTool.deleteCookie(response, DatarouterCookieKeys.userToken.toString());
	}
	
	
	/************ add/remove session from request *********/
	
	public static void addToRequest(HttpServletRequest request, DatarouterSession userSession) {
		request.setAttribute(REQUEST_ATTRIBUTE_NAME, userSession); 
	}

	public static DatarouterSession getFromRequest(HttpServletRequest request) {
		return (DatarouterSession)request.getAttribute(REQUEST_ATTRIBUTE_NAME);
	}
	
}
