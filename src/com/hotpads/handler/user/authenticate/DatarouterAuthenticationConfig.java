package com.hotpads.handler.user.authenticate;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface DatarouterAuthenticationConfig{

	String getLoginFormPath();
	String getLoginSubmitPath();
	String getUsernameParamName();
	String getPasswordParamName();
	String getLogoutPath();
	
	Iterable<BaseDatarouterAuthenticator> getAuthenticators(HttpServletRequest request,
			HttpServletResponse response);
	
	Collection<DatarouterUserRole> getRequiredRoles(String path);
	
}
