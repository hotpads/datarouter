package com.hotpads.handler.user.authenticate;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.user.authenticate.authenticator.DatarouterLoginFormAuthenticator;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;

public abstract class BaseDatarouterAuthenticationConfig
implements DatarouterAuthenticationConfig{
	
	public static final String
		PATH_LOGIN_FORM = "/login",
		PATH_LOGIN_SUBMIT = "/login/submit",
		PATH_LOGOUT = "/logout",
		PARAM_USERNAME = "loginUsername",
		PARAM_PASSWORD = "loginPassword";

	@Override
	public String getLoginFormPath(){
		return PATH_LOGIN_FORM;
	}

	@Override
	public String getLoginSubmitPath(){
		return PATH_LOGIN_SUBMIT;
	}

	@Override
	public String getUsernameParamName(){
		return PARAM_USERNAME;
	}

	@Override
	public String getPasswordParamName(){
		return PARAM_PASSWORD;
	}
	
	@Override
	public String getLogoutPath(){
		return PATH_LOGOUT;
	}
	
	public boolean isLoginRelatedPath(String path){
		return ObjectTool.equals(path, getLoginFormPath())
				|| ObjectTool.equals(path, getLoginSubmitPath())
				|| ObjectTool.equals(path, getLogoutPath());
	}
	
	@Override
	public Iterable<BaseDatarouterAuthenticator> getAuthenticators(HttpServletRequest request,
			HttpServletResponse response){

		List<DatarouterAuthenticator> authenticators = ListTool.createArrayList();
		authenticators.add(new DatarouterLoginFormAuthenticator(request, response, HotPadsAuthenticationFilter.loginSubmitURI, PARAM_USERNAME,
				PARAM_PASSWORD, routers.user(), routers.userSearch(), routers.event(), userItemRecordDao));

		authenticators.add(new SessionAuthenticator(request, response));
		authenticators.add(new RememberMeCookieAuthenticator(request, response));
		authenticators.add(new UserTokenAuthenticator(request, response));
		authenticators.add(new NewUserAuthenticator(request, response));
		return authenticators;
	}
	
}
