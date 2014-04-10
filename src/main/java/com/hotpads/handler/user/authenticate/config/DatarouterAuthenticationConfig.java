package com.hotpads.handler.user.authenticate.config;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.user.authenticate.authenticator.DatarouterAuthenticator;
import com.hotpads.handler.user.role.DatarouterUserRole;

public interface DatarouterAuthenticationConfig{

	String getSignupFormPath();
	String getSignupSubmitPath();
	
	String getSigninFormPath();
	String getSigninSubmitPath();
	
	String getSignoutPath();
	
	String getApiPath();
	
	String getUsernameParam();
	String getPasswordParam();
	String getUserRolesParam();
	String getEnabledParam();
	String getUserIdParam();
	String getApiEnabledParam();
	
	String getCookiePrefix();
	String getUserTokenCookieName();
	String getSessionTokenCookieName();
	String getTargetUrlName();

	Integer getUserTokenTimeoutSeconds();
	Integer getSessionTokenTimeoutSeconds();
	
	Iterable<DatarouterAuthenticator> getAuthenticators(HttpServletRequest request,
			HttpServletResponse response);
	
	Collection<DatarouterUserRole> getRequiredRoles(String path);
	
}
