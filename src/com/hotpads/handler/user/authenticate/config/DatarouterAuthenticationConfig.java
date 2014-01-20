package com.hotpads.handler.user.authenticate.config;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.role.DatarouterUserRole;

public interface DatarouterAuthenticationConfig{

	String getSignupFormPath();
	String getSignupSubmitPath();
	String getSigninFormPath();
	String getSigninSubmitPath();
	String getUsernameParamName();
	String getPasswordParamName();
	String getSignoutPath();
	
	Iterable<BaseDatarouterAuthenticator> getAuthenticators(HttpServletRequest request,
			HttpServletResponse response);
	
	Collection<DatarouterUserRole> getRequiredRoles(String path);
	
}
