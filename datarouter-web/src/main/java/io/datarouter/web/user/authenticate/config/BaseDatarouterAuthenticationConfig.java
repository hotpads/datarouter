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
package io.datarouter.web.user.authenticate.config;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.util.string.StringTool;
import io.datarouter.web.app.WebappName;
import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.user.DatarouterCookieKeys;
import io.datarouter.web.user.authenticate.authenticator.DatarouterAuthenticator;

public class BaseDatarouterAuthenticationConfig implements DatarouterAuthenticationConfig{

	@Inject
	private WebappName webappName;
	@Inject
	private DatarouterWebPaths paths;
	@Inject
	private DatarouterAuthenticationSettings authenticationSettings;

	@Override
	public String getKeepAlivePath(){
		return paths.keepalive.toSlashedString();
	}

	@Override
	public String getShutdownPath(){
		return paths.datarouter.shutdown.toSlashedString();
	}

	/*------------------------------- params --------------------------------*/

	@Override
	public String getUsernameParam(){
		return "username";
	}

	@Override
	public String getPasswordParam(){
		return "password";
	}

	@Override
	public String getUserRolesParam(){
		return "userRoles";
	}

	@Override
	public String getEnabledParam(){
		return "isEnabled";
	}

	@Override
	public String getUserIdParam(){
		return "userId";
	}

	@Override
	public String getSignatureParam(){
		return "signature";
	}

	@Override
	public String getNonceParam(){
		return "nonce";
	}

	@Override
	public String getTimestampParam(){
		return "timestamp";
	}

	/*------------------------------ methods --------------------------------*/

	@Override
	public List<DatarouterAuthenticator> getAuthenticators(HttpServletRequest request){
		return Collections.emptyList();
	}

	@Override
	public boolean useDatarouterAuthentication(){
		return false;
	}

	@Override
	public final Duration getUserTokenTimeoutDuration(){
		return authenticationSettings.userTokenTimeoutDuration.get().toJavaDuration();
	}

	@Override
	public final Duration getSessionTokenTimeoutDuration(){
		return authenticationSettings.sessionTokenTimeoutDuration.get().toJavaDuration();
	}

	@Override
	public String getCookiePrefix(){
		return StringTool.nullSafe(webappName.getName());
	}

	@Override
	public String getUserTokenCookieName(){
		return addCookiePrefix(DatarouterCookieKeys.userToken.toString());
	}

	@Override
	public String getSessionTokenCookieName(){
		return addCookiePrefix(DatarouterCookieKeys.sessionToken.toString());
	}

	@Override
	public String getTargetUrlName(){
		return addCookiePrefix(DatarouterCookieKeys.targetUrl.toString());
	}

	private String addCookiePrefix(String cookieName){
		String prefix = getCookiePrefix();
		if(StringTool.isEmpty(prefix)){
			return cookieName;
		}
		return prefix + StringTool.capitalizeFirstLetter(cookieName);
	}

	public static String normalizePath(String rawPath){
		Objects.requireNonNull(rawPath);
		String path = rawPath.trim().toLowerCase();
		//not scrubbing out duplicate slashes.  should we?
		if(path.length() > 1 && path.endsWith("/")){
			return path.substring(0, path.length() - 1);//remove trailing slash
		}
		return path;
	}

	public static boolean pathAContainsB(String rawA, String rawB){
		String normalizedA = normalizePath(rawA);
		String normalizedB = normalizePath(rawB);
		if(normalizedA.equals(normalizedB)){
			return true;
		}

		// a=/fl should NOT contain b=/flowbee
		String aAsDirectory = normalizedA + "/";
		return normalizedB.startsWith(aAsDirectory);
	}

}
