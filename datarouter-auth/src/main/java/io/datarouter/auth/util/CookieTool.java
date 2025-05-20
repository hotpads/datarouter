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
package io.datarouter.auth.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.number.NumberTool;
import io.datarouter.util.string.StringTool;

public class CookieTool{

	public static void addCookie(HttpServletResponse response, String cookieName, String value, String path,
			long maxAgeSeconds){
		Cookie cookie = new Cookie(cookieName, value);
		if(path != null){
			cookie.setPath(path);
		}
		cookie.setMaxAge(NumberTool.limitLongToIntRange(maxAgeSeconds));
		cookie.setHttpOnly(true); //enforce HttpOnly cookies (can't be accessed by javascript) to prevent XSS attacks
		cookie.setSecure(true);
		response.addCookie(cookie);
	}

	public static void addCookie(HttpServletResponse response, String cookieName, String value, String path,
			long maxAgeSeconds, boolean sameSiteNone){
		if(sameSiteNone){
			addCookieSameSiteNone(response, cookieName, value, path, maxAgeSeconds);
		}else{
			addCookie(response, cookieName, value, path, maxAgeSeconds);
		}
	}

	public static void addCookieSameSiteNone(HttpServletResponse response, String cookieName, String value, String path,
			long maxAgeSeconds){
		String cookie = String.join("; ",
				cookieName + '=' + value,
				"Path=" + path,
				"Max-Age=" + NumberTool.limitLongToIntRange(maxAgeSeconds),
				"HttpOnly",
				"SameSite=None",
				"Secure");
		response.addHeader("Set-Cookie", cookie);
	}

	public static String getCookieValue(HttpServletRequest request, String cookieName){
		// Don't build a useless map every time we look for a cookie:
		Cookie cookie = getCookie(request.getCookies(), cookieName);
		return cookie == null ? null : cookie.getValue();
	}

	public static String getCookieValue(Cookie[] cookies, String cookieName, String defaultValue){
		Cookie cookie = getCookie(cookies,cookieName);
		if(cookie == null){
			return defaultValue;
		}
		return cookie.getValue();
	}

	public static Boolean getCookieBoolean(Cookie[] cookies, String cookieName){
		String value = getCookieValue(cookies, cookieName, null);
		if(value == null){
			return null;
		}
		try{
			return Boolean.parseBoolean(value);
		}catch(Exception e){
			//swallow
		}
		return null;
	}

	public static Boolean getCookieBooleanDefault(Cookie[] cookies, String cookieName, Boolean defaultValue){
		String value = getCookieValue(cookies, cookieName, null);
		if(value == null){
			return defaultValue;
		}
		try{
			return Boolean.parseBoolean(value);
		}catch(Exception e){
			//swallow
		}
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

	public static Cookie getCookie(Cookie[] cookies, String cookieName){
		if(cookies != null && cookieName != null){
			for(Cookie cookie : cookies){
				if(cookieName.equals(cookie.getName())){
					return cookie;
				}
			}
		}
		return null;
	}

	public static Optional<String> getFormattedCookie(Cookie[] cookies, String cookieName, String regex,
			String replacement){
		return Scanner.of(cookies)
				.include(cookie -> cookieName.equals(cookie.getName()))
				.map(Cookie::getValue)
				.map(value -> value.replaceFirst(regex, replacement))
				.map(value -> URLDecoder.decode(value, StandardCharsets.UTF_8))
				.findFirst();
	}

	public static void deleteCookie(HttpServletResponse response, String cookieName){
		Cookie cookieToKill = new Cookie(cookieName, "");
		cookieToKill.setPath("/");
		cookieToKill.setMaxAge(0);
		response.addCookie(cookieToKill);
	}

	/**
	 * Build a map from the string with the format "
	 * <code>key[keyValueSeparator]value[entrySeparator]key[keyValueSeparator]value...</code>"
	 * @param string The input {@link String}
	 * @param entrySeparator The separator between tow entries
	 * @param keyValueSeparator The separator between the key and the value
	 * @return a {@link Map}
	 */
	public static Map<String,String> getMapFromString(String string, String entrySeparator, String keyValueSeparator){
		Map<String,String> map = new TreeMap<>();
		if(StringTool.isEmpty(string)){
			return map;
		}
		String[] entries = string.split(entrySeparator);
		String[] keyVal;
		for(String entry : entries){
			if(StringTool.notEmpty(entry)){
				keyVal = entry.split(keyValueSeparator);
				map.put(keyVal[0], keyVal.length > 1 ? keyVal[1] : null);
			}
		}
		return map;
	}

}
