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
package io.datarouter.web.user.session;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.util.net.UrlTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.util.RequestAttributeKey;
import io.datarouter.web.util.RequestAttributeTool;
import io.datarouter.web.util.http.CookieTool;

@Singleton
public class DatarouterSessionManager{

	public static final RequestAttributeKey<DatarouterSession> DATAROUTER_SESSION_ATTRIBUTE = new RequestAttributeKey<>(
			"datarouterSession");
	private static final int TARGET_URL_COOKIE_EXPIRATION_SECONDS = 30 * 60;

	@Inject
	private DatarouterAuthenticationConfig config;


	/*---------------------------- target url -------------------------------*/

	public void addTargetUrlCookie(HttpServletResponse response, String targetUrl){
		CookieTool.addCookie(response, config.getTargetUrlName(), UrlTool.encode(targetUrl), "/",
				TARGET_URL_COOKIE_EXPIRATION_SECONDS);
	}

	public URL getTargetUrlFromCookie(HttpServletRequest request){
		String targetUrlString = CookieTool.getCookieValue(request, config.getTargetUrlName());
		if(StringTool.isEmpty(targetUrlString)){
			return null;
		}
		targetUrlString = UrlTool.decode(targetUrlString);
		try{
			return new URL(targetUrlString);
		}catch(MalformedURLException e){
			throw new IllegalArgumentException("invalid targetUrl:" + targetUrlString);
		}
	}

	public void clearTargetUrlCookie(HttpServletResponse response){
		CookieTool.deleteCookie(response, config.getTargetUrlName());
	}


	/*---------------------------- session token ----------------------------*/

	public void addSessionTokenCookie(HttpServletResponse response, String sessionToken){
		CookieTool.addCookie(response, config.getSessionTokenCookieName(), sessionToken, "/", config
				.getSessionTokenTimeoutDuration().getSeconds());
	}

	public String getSessionTokenFromCookie(HttpServletRequest request){
		return CookieTool.getCookieValue(request, config.getSessionTokenCookieName());
	}

	public void clearSessionTokenCookie(HttpServletResponse response){
		CookieTool.deleteCookie(response, config.getSessionTokenCookieName());
	}


	/*---------------------------- user token -------------------------------*/

	public void addUserTokenCookie(HttpServletResponse response, String userToken){
		CookieTool.addCookie(response, config.getUserTokenCookieName(), userToken, "/", config
				.getUserTokenTimeoutDuration().getSeconds());
	}

	public String getUserTokenFromCookie(HttpServletRequest request){
		return CookieTool.getCookieValue(request, config.getUserTokenCookieName());
	}

	public void clearUserTokenCookie(HttpServletResponse response){
		CookieTool.deleteCookie(response, config.getUserTokenCookieName());
	}


	/*--------------------- update session from request ---------------------*/

	public static void addToRequest(ServletRequest request, DatarouterSession userSession){
		RequestAttributeTool.set(request, DATAROUTER_SESSION_ATTRIBUTE, userSession);
	}

	public static Optional<DatarouterSession> getFromRequest(ServletRequest request){
		return RequestAttributeTool.get(request, DATAROUTER_SESSION_ATTRIBUTE);
	}

}
