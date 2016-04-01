package com.hotpads.util.http.response;

import java.util.List;

import org.apache.http.cookie.Cookie;

public class CookieTool {

	public static String getCookieValue(List<Cookie> cookies, String cookieName) {
		Cookie cookie = getCookie(cookies, cookieName);
		if(cookie == null) {
			return null;
		}
		return cookie.getValue();
	}

	public static Cookie getCookie(List<Cookie> cookies, String cookieName) {
		if (cookies == null || cookieName == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookieName.equals(cookie.getName())){
				return cookie;
			}
		}
		return null;
	}

}
