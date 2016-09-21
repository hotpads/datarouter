package com.hotpads.handler.user.authenticate.authenticator.impl;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByUsernameLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.DatarouterPasswordService;
import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.util.core.exception.IncorrectPasswordException;
import com.hotpads.util.core.exception.InvalidCredentialsException;
import com.hotpads.util.http.RequestTool;

public class DatarouterSigninFormAuthenticator extends BaseDatarouterAuthenticator{
	private DatarouterAuthenticationConfig authenticationConfig;
	private DatarouterUserNodes userNodes;
	private DatarouterPasswordService passwordService;

	public DatarouterSigninFormAuthenticator(HttpServletRequest request, HttpServletResponse response,
			DatarouterAuthenticationConfig authenticationConfig, DatarouterUserNodes userNodes,
			DatarouterPasswordService passwordService) {
		super(request, response);
		this.authenticationConfig = authenticationConfig;
		this.userNodes = userNodes;
		this.passwordService = passwordService;
	}

	@Override
	public DatarouterSession getSession(){
		//the usual case where we're not submitting the login form.  just skip this filter
		if(DrObjectTool.notEquals(request.getServletPath(), authenticationConfig.getSigninSubmitPath())){
			return null;
		}

		String username = RequestTool.get(request, authenticationConfig.getUsernameParam(), null);
		String password = RequestTool.get(request, authenticationConfig.getPasswordParam(), null);

		DatarouterUser user = lookupAndValidateUser(username, password);

		user.setLastLoggedIn(new Date());
		userNodes.getUserNode().put(user, null);

		DatarouterSession session = DatarouterSession.createFromUser(user);
		return session;
	}


	private DatarouterUser lookupAndValidateUser(String username, String password){
		if(DrStringTool.isEmpty(username)){
			throw new InvalidCredentialsException("no username specified");
		}

		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByUsernameLookup(username), null);

		if(user==null){
			throw new InvalidCredentialsException("user not found ("+username+")");
		}
		if(DrBooleanTool.isFalseOrNull(user.getEnabled())){
			throw new InvalidCredentialsException("user not enabled ("+username+")");
		}
		if(DrStringTool.isEmpty(password)){
			throw new InvalidCredentialsException("password cannot be empty ("+username+")");
		}
		if( ! passwordService.isPasswordCorrect(user, password)){
			throw new IncorrectPasswordException("invalid password ("+username+")");
		}
		return user;
	}

}
