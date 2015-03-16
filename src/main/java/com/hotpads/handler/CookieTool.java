package com.hotpads.handler;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieTool{

	public static void addCookie(HttpServletResponse response, String cookieName, String value, @Nullable String path, int maxAge){
		Cookie cookie = new Cookie(cookieName, value);
		if (path != null) {
			cookie.setPath(path);
		}
		cookie.setMaxAge(maxAge);
		cookie.setHttpOnly(true); //enforce HttpOnly cookies (can't be accessed by javascript) to prevent XSS attacks
		response.addCookie(cookie);
	}
	
	public static String getCookieValue(HttpServletRequest request, String cookieName){
		// Don't build a useless map every time we look for a cookie:
		Cookie cookie = getCookie( request.getCookies(), cookieName );
		return ( cookie == null ) ? null : cookie.getValue();
	}
	
	public static String getCookieValue(Cookie[] cookies, String cookieName,String defaultValue) {
		Cookie cookie = getCookie(cookies,cookieName);
		if(cookie==null) {
			return defaultValue;
		}
		return cookie.getValue();
	}
	
	public static Boolean getCookieBoolean(Cookie[] cookies, String cookieName){
		String v = getCookieValue(cookies, cookieName, null);
		if(v == null) return null;
		try{
			return Boolean.parseBoolean(v);
		}catch(Exception e){}
		return null;
	}
	
	public static Boolean getCookieBooleanDefault(Cookie[] cookies, String cookieName, Boolean defaultValue){
		String v = getCookieValue(cookies, cookieName, null);
		if(v == null) return defaultValue;
		try{
			return Boolean.parseBoolean(v);
		}catch(Exception e){}
		return defaultValue;
	}

	public static String escapeCookieCharacters(String value){
		value = value.replace("%", "%25");
		value = value.replace(" ", "%20");
		value = value.replace(",", "%2C");
		value = value.replace(";", "%3B");
		value = value.replace("=", "%3D");
		return value;
	}
	
	public static String unescapeCookieCharacters(String value){
		value = value.replace("%3B", ";");
		value = value.replace("%2C", ",");
		value = value.replace("%20", " ");
		value = value.replace("%25", "%");
		value = value.replace("%3D", "=");
		return value;
	}

	public static Cookie getCookie(Cookie[] cookies, String cookieName) {
		if (cookies != null && cookieName!=null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cookieName.equals(cookie.getName())){
					return (cookie);
				}
			}
		}
		return (null);
	}

	public static void deleteCookie(HttpServletResponse response, String cookieName){
		Cookie cookieToKill = new Cookie(cookieName, "");
		cookieToKill.setPath("/");
		cookieToKill.setMaxAge(0);
		response.addCookie(cookieToKill);
	}

	public static Map<String, String> getCookieMap(HttpServletRequest request){
		Map<String, String> map = new HashMap<String, String>();
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			Cookie cookie;
			for (int i = 0; i < cookies.length; i++) {
				cookie = cookies[i];
				if(cookie==null || cookie.getName()==null){ 
					continue;
				}
				map.put(cookie.getName(), cookie.getValue());
			}
		}
		return map;
	}
}
