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
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.util.lang.ObjectTool;
import io.datarouter.util.time.DurationTool;
import io.datarouter.web.user.authenticate.authenticator.DatarouterAuthenticator;
import io.datarouter.web.user.session.service.Session;

public interface DatarouterAuthenticationConfig{

	String getHomePath();
	String getKeepAlivePath();
	String getDatarouterPath();
	String getShutdownPath();

	String getSignupPath();
	String getSignupSubmitPath();
	String getSigninPath();
	String getSigninSubmitPath();
	String getSignoutPath();
	default String getPermissionRequestPath(){
		return "/permissionRequest";
	}

	String getResetPasswordPath();
	String getResetPasswordSubmitPath();

	String getAdminPath();
	String getViewUsersPath();
	String getListUsersPath();
	String getCreateUserPath();
	String getEditUserPath();
	String getCreateUserSubmitPath();
	String getEditUserSubmitPath();
	String getAccountManagerPath();

	String getUsernameParam();
	String getPasswordParam();
	String getUserRolesParam();
	String getEnabledParam();
	String getUserIdParam();
	String getSignatureParam();
	String getNonceParam();
	String getTimestampParam();

	String getHomeJsp();
	String getViewUsersJsp();
	String getCreateUserJsp();
	String getEditUserJsp();
	String getResetPasswordJsp();

	String getCookiePrefix();
	String getUserTokenCookieName();
	String getSessionTokenCookieName();
	String getTargetUrlName();

	Duration getUserTokenTimeoutDuration();
	Duration getSessionTokenTimeoutDuration();

	default boolean isSessionExpired(Session session){
		ObjectTool.requireNonNulls(session, session.getUpdated());
		return getSessionTokenTimeoutDuration().minus(DurationTool.sinceDate(session.getUpdated())).isNegative();
	}

	List<DatarouterAuthenticator> getAuthenticators(HttpServletRequest request);

	boolean useDatarouterAuthentication();

}
