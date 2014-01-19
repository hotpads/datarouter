package com.hotpads.handler.user.session;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.CookieTool;
import com.hotpads.handler.DatarouterCookieKeys;
import com.hotpads.util.core.StringTool;

public class DatarouterSessionTool{
	
	/************* static fields **********************/

	private final static String REQUEST_ATTRIBUTE_NAME = "userSession";
	private static final int 
		TARGET_URL_COOKIE_EXPIRATION_SECONDS = 30 * 60,
		SESSION_TOKEN_COOKIE_EXPIRATION_SECONDS = 30 * 60,
		USER_TOKEN_COOKIE_EXPIRATION_SECONDS = 365 * 24 * 3600;
		
	
	/************* static methods *********************/

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
	
	public static void addSessionTokenCookie(HttpServletResponse response, String sessionToken){
		CookieTool.addCookie(response, DatarouterCookieKeys.sessionToken.toString(), sessionToken, "/", 
				SESSION_TOKEN_COOKIE_EXPIRATION_SECONDS);
	}
	
	public static void addUserTokenCookie(HttpServletResponse response, String userToken){
		CookieTool.addCookie(response, DatarouterCookieKeys.userToken.toString(), userToken, "/", 
				USER_TOKEN_COOKIE_EXPIRATION_SECONDS);
	}
	
	public static String getSessionTokenFromCookie(HttpServletRequest request){
		return CookieTool.getCookieValue(request, DatarouterCookieKeys.sessionToken.toString());
	}

	public static DatarouterSession getFromRequest(HttpServletRequest request) {
		return (DatarouterSession)request.getAttribute(REQUEST_ATTRIBUTE_NAME);
	}
	
	public static void addToRequest(HttpServletRequest request, DatarouterSession userSession) {
		request.setAttribute(REQUEST_ATTRIBUTE_NAME, userSession); 
	}
	
}
