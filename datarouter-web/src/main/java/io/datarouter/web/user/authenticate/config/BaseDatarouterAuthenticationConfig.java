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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Preconditions;

import io.datarouter.util.string.StringTool;
import io.datarouter.web.app.WebappName;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.user.DatarouterCookieKeys;
import io.datarouter.web.user.authenticate.authenticator.DatarouterAuthenticator;

public class BaseDatarouterAuthenticationConfig implements DatarouterAuthenticationConfig{

	private static final String PATH_home = "/";
	protected static final String PATH_keepalive = "/keepalive";
	private static final String PATH_datarouter = "/datarouter";
	private static final String PATH_shutdown = "/shutdown";

	private static final String PATH_signup = "/signup";
	private static final String PATH_signupSubmit = PATH_signup + "/submit";
	private static final String PATH_signin = "/signin";
	private static final String PATH_signinSubmit = PATH_signin + "/submit";
	private static final String PATH_signout = "/signout";

	private static final String PATH_resetPassword = "/resetPassword";
	private static final String PATH_resetPasswordSubmit = "/resetPasswordSubmit";

	private static final String PATH_admin = "/admin";
	private static final String PATH_viewUsers = PATH_admin + "/viewUsers";
	private static final String PATH_listUsers = PATH_admin + "/listUsers";
	private static final String PATH_createUser = PATH_admin + "/createUser";
	private static final String PATH_createUserSubmit = PATH_admin + "/createUserSubmit";
	private static final String PATH_editUser = PATH_admin + "/editUser";
	private static final String PATH_editUserSubmit = PATH_admin + "/editUserSubmit";
	private static final String PATH_accountManager = PATH_admin + "/accounts";

	private static final String PARAM_username = "username";
	private static final String PARAM_password = "password";
	private static final String PARAM_userRoles = "userRoles";
	private static final String PARAM_enabled = "isEnabled";
	private static final String PARAM_userId = "userId";
	private static final String PARAM_signature = "signature";
	private static final String PARAM_nonce = "nonce";
	private static final String PARAM_timestamp = "timestamp";

	private static final String JSP_home = "/WEB-INF/jsp/home.jsp";//file doesn't exist in datarouter-web

	@Inject
	private WebappName webappName;
	@Inject
	private DatarouterWebFiles files;
	@Inject
	private DatarouterAuthenticationSettings datarouterAuthenticationSettings;

	@Override
	public String getHomePath(){
		return PATH_home;
	}

	@Override
	public String getKeepAlivePath(){
		return PATH_keepalive;
	}

	@Override
	public String getDatarouterPath(){
		return PATH_datarouter;
	}

	@Override
	public String getShutdownPath(){
		return PATH_datarouter + PATH_shutdown;
	}

	/*--------------------------- signin/out/up -----------------------------*/

	@Override
	public String getSignupPath(){
		return PATH_signup;
	}

	@Override
	public String getSignupSubmitPath(){
		return PATH_signupSubmit;
	}

	@Override
	public String getSigninPath(){
		return PATH_signin;
	}

	@Override
	public String getSigninSubmitPath(){
		return PATH_signinSubmit;
	}

	@Override
	public String getSignoutPath(){
		return PATH_signout;
	}

	/*------------------------------ password -------------------------------*/

	@Override
	public String getResetPasswordPath(){
		return PATH_resetPassword;
	}

	@Override
	public String getResetPasswordSubmitPath(){
		return PATH_resetPasswordSubmit;
	}

	/*------------------------------- admin ---------------------------------*/


	@Override
	public String getAdminPath(){
		return PATH_admin;
	}

	@Override
	public String getViewUsersPath(){
		return PATH_viewUsers;
	}

	@Override
	public String getListUsersPath(){
		return PATH_listUsers;
	}

	@Override
	public String getCreateUserPath(){
		return PATH_createUser;
	}

	@Override
	public String getCreateUserSubmitPath(){
		return PATH_createUserSubmit;
	}

	@Override
	public String getEditUserPath(){
		return PATH_editUser;
	}

	@Override
	public String getEditUserSubmitPath(){
		return PATH_editUserSubmit;
	}

	@Override
	public String getAccountManagerPath(){
		return PATH_accountManager;
	}

	/*------------------------------- params --------------------------------*/

	@Override
	public String getUsernameParam(){
		return PARAM_username;
	}

	@Override
	public String getPasswordParam(){
		return PARAM_password;
	}

	@Override
	public String getUserRolesParam(){
		return PARAM_userRoles;
	}

	@Override
	public String getEnabledParam(){
		return PARAM_enabled;
	}

	@Override
	public String getUserIdParam(){
		return PARAM_userId;
	}

	@Override
	public String getSignatureParam(){
		return PARAM_signature;
	}

	@Override
	public String getNonceParam(){
		return PARAM_nonce;
	}

	@Override
	public String getTimestampParam(){
		return PARAM_timestamp;
	}

	/*-------------------------------- jsp ----------------------------------*/

	@Override
	public String getHomeJsp(){
		return JSP_home;
	}

	@Override
	public String getViewUsersJsp(){
		return files.jsp.authentication.viewUsersJsp.toSlashedString();
	}

	@Override
	public String getCreateUserJsp(){
		return files.jsp.authentication.createUserFormJsp.toSlashedString();
	}

	@Override
	public String getEditUserJsp(){
		return files.jsp.authentication.editUserFormJsp.toSlashedString();
	}

	@Override
	public String getResetPasswordJsp(){
		return files.jsp.authentication.resetPasswordFormJsp.toSlashedString();
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
		return datarouterAuthenticationSettings.userTokenTimeoutDuration.get().toJavaDuration();
	}

	@Override
	public final Duration getSessionTokenTimeoutDuration(){
		return datarouterAuthenticationSettings.sessionTokenTimeoutDuration.get().toJavaDuration();
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
		Preconditions.checkNotNull(rawPath);
		String path = rawPath.trim().toLowerCase();
		//not scrubbing out duplicate slashes.  should we?
		if(path.length() > 1 && path.endsWith("/")){
			return path.substring(0, path.length() - 1);//remove trailing slash
		}
		return path;
	}

	public boolean isLoginRelatedPath(String path){
		return pathAContainsB(getSignupPath(), path)
				|| pathAContainsB(getSignupSubmitPath(), path)
				|| pathAContainsB(getSigninPath(), path)
				|| pathAContainsB(getSigninSubmitPath(), path)
				|| pathAContainsB(getSignoutPath(), path);
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


	/*------------------------------- tests ---------------------------------*/

	public static class BaseDatarouterAuthenticationConfigTests{

		@Test
		public void testNormalize(){
			Assert.assertEquals(normalizePath(""), "");
			Assert.assertEquals(normalizePath("/"), "/");
			Assert.assertEquals(normalizePath(" / "), "/");
			Assert.assertEquals(normalizePath("/caterpillar"), "/caterpillar");
			Assert.assertEquals(normalizePath("/caterpillar/"), "/caterpillar");
			Assert.assertEquals(normalizePath("/caterpillar//"), "/caterpillar/");//prob not valid
		}

		@Test
		public void testContains(){
			Assert.assertTrue(pathAContainsB("/fl", "/fl"));
			Assert.assertTrue(pathAContainsB("/fl", "/fl/"));
			Assert.assertTrue(pathAContainsB("/fl/", "/fl/"));
			Assert.assertTrue(pathAContainsB("/fl", "/fl/owbee"));
			Assert.assertFalse(pathAContainsB("/fl", "/flowbee"));
			Assert.assertFalse(pathAContainsB("/flowbee", "/fl"));
		}

	}

}
