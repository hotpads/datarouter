/**
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.datarouter.util.string.StringTool;
import io.datarouter.web.user.authenticate.config.DatarouterAuthenticationConfig;
import io.datarouter.web.util.http.CookieTool;

@Singleton
public class DatarouterSessionManager{

	@Inject
	private DatarouterAuthenticationConfig config;

	/************* static fields **********************/

	public static final String REQUEST_ATTRIBUTE_NAME = "datarouterSession";
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
		if(StringTool.isEmpty(targetUrlString)){
			return null;
		}
		try{
			return new URL(targetUrlString);
		}catch(MalformedURLException e){
			throw new IllegalArgumentException("invalid targetUrl:" + targetUrlString);
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

	public static void addToRequest(HttpServletRequest request, DatarouterSession userSession){
		request.setAttribute(REQUEST_ATTRIBUTE_NAME, userSession);
	}

	public static Optional<DatarouterSession> getFromRequest(HttpServletRequest request){
		return Optional.ofNullable((DatarouterSession)request.getAttribute(REQUEST_ATTRIBUTE_NAME));
	}

}
