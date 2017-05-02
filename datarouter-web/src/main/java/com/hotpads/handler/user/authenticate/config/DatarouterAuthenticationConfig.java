package com.hotpads.handler.user.authenticate.config;

import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.dispatcher.DispatchRule;
import com.hotpads.handler.user.authenticate.authenticator.DatarouterAuthenticator;
import com.hotpads.handler.user.role.DatarouterUserRole;

public interface DatarouterAuthenticationConfig{

	String getHomePath();
	String getKeepAlivePath();
	String getDatarouterPath();
	String getShutdownPath();

	String getApiPath();

	String getSignupPath();
	String getSignupSubmitPath();
	String getSigninPath();
	String getSigninSubmitPath();
	String getSignoutPath();

	String getResetPasswordPath();
	String getResetPasswordSubmitPath();

	String getAdminPath();
	String getViewUsersPath();
	String getCreateUserPath();
	String getEditUserPath();
	String getResetApiKeySubmitPath();
	String getResetSecretKeySubmitPath();
	String getCreateUserSubmitPath();
	String getEditUserSubmitPath();
	String getAccountManagerPath();

	String getApiKeyParam();
	String getUsernameParam();
	String getPasswordParam();
	String getUserRolesParam();
	String getEnabledParam();
	String getUserIdParam();
	String getApiEnabledParam();
	String getSignatureParam();
	String getNonceParam();
	String getTimestampParam();

	String getKeepaliveJsp();
	String getHomeJsp();
	String getViewUsersJsp();
	String getCreateUserJsp();
	String getEditUserJsp();
	String getResetPasswordJsp();

	String getCookiePrefix();
	String getUserTokenCookieName();
	String getSessionTokenCookieName();
	String getTargetUrlName();

	Integer getUserTokenTimeoutSeconds();
	Integer getSessionTokenTimeoutSeconds();

	Iterable<DatarouterAuthenticator> getAuthenticators(HttpServletRequest request,
			HttpServletResponse response);

	/**
	 * @deprecated use {@link DispatchRule#allowAnonymous()} and {@link DispatchRule#allowRoles(DatarouterUserRole...)}
	 */
	@Deprecated
	default Collection<DatarouterUserRole> getRequiredRoles(String path){
		return Collections.emptyList();
	}

	boolean useDatarouterAuthentication();
}
