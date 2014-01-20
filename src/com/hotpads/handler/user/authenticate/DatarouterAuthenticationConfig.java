package com.hotpads.handler.user.authenticate;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
