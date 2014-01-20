package com.hotpads.handler.user.authenticate.authenticator;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hsqldb.User;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByEmailLookup;
import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.authenticate.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.authenticate.DatarouterPasswordService;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.exception.IncorrectPasswordException;
import com.hotpads.util.core.exception.InvalidCredentialsException;

public class DatarouterLoginFormAuthenticator extends BaseDatarouterAuthenticator{
	private static Logger logger = Logger.getLogger(DatarouterLoginFormAuthenticator.class);

	private DatarouterAuthenticationConfig authenticationConfig;
	private DatarouterUserNodes userNodes;
	private DatarouterPasswordService passwordService;
	
	public DatarouterLoginFormAuthenticator(HttpServletRequest request, HttpServletResponse response, 
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
		
		String username = RequestTool.get(request, authenticationConfig.getUsernameParamName(), null);
		String password = RequestTool.get(request, authenticationConfig.getPasswordParamName(), null);
		
		DatarouterUser user = lookupAndValidateUser(username, password);
		
		user.setLastLoggedIn(new Date());
		userNodes.getUserNode().put(user, null);
		
		DatarouterSession session = DatarouterSession.createFromUser(user);
		return session;
	}
	
	private DatarouterUser lookupAndValidateUser(String username, String password){
		if(StringTool.isEmpty(username)){
			throw new InvalidCredentialsException("no username specified");
		}
		
		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByEmailLookup(username), null);
		
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
	
	/**
	 * Fully login a user: building a userSession for them and storing it in memcached, 
	 * adding their userToken to their cookie, and adding a rememberme cookie if desired
	 * @param user
	 * @param request
	 * @param response
	 * @param rememberMe whether or not to add a rememberme cookie
	 * @return
	 */
	public static DatarouterSession loginUser(User user, 
										  HttpServletRequest request, 
										  HttpServletResponse response, 
										  boolean rememberMe) {
		DatarouterSession userSession = buildUserSessionForUser(user, request, response);
		DatarouterSession.store(request, userSession);
		
		if (rememberMe) {
			RememberMeCookieAuthenticator.addRememberMeCookieToResponse(request, response, user);
		}
		
		UserSessionTokenTool.addUserTokenCookie(response, userSession.getUserToken());
		UserSessionTokenTool.addSessionTokenCookie(response, userSession.getSessionToken());
		
		return userSession;
	}


	public static DatarouterSession buildUserSessionForUser(User user, HttpServletRequest request, HttpServletResponse response){
		DatarouterSession userSession = new DatarouterSession();
		
		userSession.setId(user.getId());
		userSession.setEmail(user.getEmail());
		userSession.setAnonUser(false);
		userSession.setUserToken(user.getToken());
		userSession.setSessionToken(UserTool.getSessionToken(request));
		if (userSession.getSessionToken() == null) {
			logger.error("null session token found, creating new one");
			String newSessionToken = UserSessionTokenTool.buildSessionToken();
			userSession.setSessionToken(newSessionToken);
			UserSessionTokenTool.addSessionTokenCookie(response, userSession.getSessionToken());
		}
		if (user.getCreationDate() != null) {
			userSession.setUserCreationDate(user.getCreationDate()); //experimentsTODO this is not quite when they first used hotpads 
		}
		
		List<Authority> authorities = AuthorityDao.getAuthorities(user.getKey());
		Set<UserRole> userRoles = Authority.getUserRoles(authorities);
		userSession.setUserRoles(userRoles);
		
		return userSession;
	}
	
	
}
