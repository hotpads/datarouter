package com.hotpads.handler.user.authenticate.authenticator.impl;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByUsernameLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.DatarouterPasswordService;
import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.exception.IncorrectPasswordException;
import com.hotpads.util.core.exception.InvalidCredentialsException;

public class DatarouterSigninFormAuthenticator extends BaseDatarouterAuthenticator{
//	private static Logger logger = Logger.getLogger(DatarouterLoginFormAuthenticator.class);

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
		if(ObjectTool.notEquals(request.getServletPath(), authenticationConfig.getSigninSubmitPath())){
			return null; 
		}
		
		String username = RequestTool.get(request, authenticationConfig.getUsernameParam(), null);
		String password = RequestTool.get(request, authenticationConfig.getPasswordParam(), null);
		
		DatarouterUser user = lookupAndValidateUser(username, password);
		
		user.setLastLoggedIn(new Date());
		userNodes.getUserNode().put(user, null);

//		if (rememberMe) {
//			RememberMeCookieAuthenticator.addRememberMeCookieToResponse(request, response, user);
//		}
		
		DatarouterSession session = DatarouterSession.createFromUser(user);
		return session;
	}
	
	
	private DatarouterUser lookupAndValidateUser(String username, String password){
		if(StringTool.isEmpty(username)){
			throw new InvalidCredentialsException("no username specified");
		}
		
		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByUsernameLookup(username), null);
		
		if(user==null){
			throw new InvalidCredentialsException("user not found ("+username+")");
		}
		if(BooleanTool.isFalseOrNull(user.isEnabled())){
			throw new InvalidCredentialsException("user not enabled ("+username+")");
		}
		if(StringTool.isEmpty(password)){
			throw new InvalidCredentialsException("password cannot be empty ("+username+")");
		}
		if( ! passwordService.isPasswordCorrect(user, password)){
			throw new IncorrectPasswordException("invalid password ("+username+")");
		}
		return user;
	}
		
}
