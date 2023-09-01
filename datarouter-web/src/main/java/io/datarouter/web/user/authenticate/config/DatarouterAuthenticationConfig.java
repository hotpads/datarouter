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
package io.datarouter.web.user.authenticate.config;

import java.time.Duration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.auth.authenticate.authenticator.DatarouterAuthenticator;
import io.datarouter.auth.session.Session;
import io.datarouter.util.Require;
import io.datarouter.util.time.DurationTool;
import io.datarouter.web.config.DatarouterWebPaths;

// TODO braydonh figure out how to move this out of dr-web
public interface DatarouterAuthenticationConfig{

	DatarouterWebPaths PATHS = new DatarouterWebPaths();

	String getKeepAlivePath();
	String getShutdownPath();

	default String getSigninPath(){
		return PATHS.signin.toSlashedString();
	}

	default String getSigninSubmitPath(){
		return PATHS.signin.submit.toSlashedString();
	}

	default String getPermissionRequestPath(){
		return PATHS.permissionRequest.toSlashedString();
	}

	String getUsernameParam(); // used in jsps
	String getPasswordParam(); // used in jsps
	String getUserRolesParam();
	String getEnabledParam();
	String getSignatureParam();
	String getNonceParam();
	String getTimestampParam();

	String getCookiePrefix();
	String getUserTokenCookieName();
	String getSessionTokenCookieName();
	String getTargetUrlName();

	Duration getUserTokenTimeoutDuration();
	Duration getSessionTokenTimeoutDuration();

	default boolean useSameSiteNone(){
		return false;
	}

	default boolean isSessionExpired(Session session){
		Require.noNulls(session, session.getUpdated());
		return getSessionTokenTimeoutDuration().minus(DurationTool.sinceDate(session.getUpdated())).isNegative();
	}

	List<DatarouterAuthenticator> getAuthenticators(HttpServletRequest request);

	boolean useDatarouterAuthentication();

}
