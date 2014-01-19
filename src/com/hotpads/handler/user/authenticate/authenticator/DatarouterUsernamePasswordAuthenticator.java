package com.hotpads.handler.user.authenticate.authenticator;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hotpads.analytics.event.EventRouter;
import com.hotpads.dao.UserSearchRouter;
import com.hotpads.dao.user.search.UserItemRecordDao;
import com.hotpads.databean.property.user.User;
import com.hotpads.databean.property.user.UserSession;
import com.hotpads.databean.property.user.security.Authority;
import com.hotpads.databean.property.user.security.UserRole;
import com.hotpads.databean.property.user.util.UserPasswordEncoder;
import com.hotpads.databean.property.user.util.UserSessionTokenTool;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.user.AuthorityDao;
import com.hotpads.user.UserDao;
import com.hotpads.user.UserRouter;
import com.hotpads.util.core.StringTool;
import com.hotpads.util.core.exception.IncorrectPasswordException;
import com.hotpads.util.core.exception.InvalidCredentialsException;
import com.hotpads.util.core.exception.UsernameNotFoundException;
import com.hotpads.websupport.authentication.BaseAuthenticator;
import com.hotpads.websupport.user.UserTool;
import com.hotpads.websupport.util.RobotDetector;
import com.hotpads.websupport.util.ServletUtils;

public class DatarouterUsernamePasswordAuthenticator extends BaseAuthenticator{
	protected static Logger logger = Logger.getLogger(DatarouterUsernamePasswordAuthenticator.class);

	protected String loginSubmitUri;
	protected String userNameArg;
	protected String passwordArg;
	protected UserDao userDao;
	protected AuthorityDao authorityDao;
	protected UserRouter userRouter;
	protected UserItemRecordDao userItemRecordDao;
	
	public DatarouterUsernamePasswordAuthenticator(HttpServletRequest request, HttpServletResponse response, 
			String loginSubmitUri, String userName, String password,
			UserRouter userRouter, UserSearchRouter userSearchRouter, EventRouter eventRouter, 
			UserItemRecordDao userItemRecordDao) {
		super(request, response);
		this.loginSubmitUri = loginSubmitUri;
		this.userNameArg = userName;
		this.passwordArg = password;
		this.userDao = new UserDao(userRouter, userSearchRouter, eventRouter, userItemRecordDao);
		this.authorityDao = new AuthorityDao(userRouter);
		this.userRouter = userRouter;
		this.userItemRecordDao = userItemRecordDao;
	}
	
	@Override
	public DatarouterSession getUserSession(){
		String uri = request.getRequestURI();
		if( ! loginSubmitUri.equals(uri)){ 
			return null; 
		}
		
		String username = RequestTool.get(request, userNameArg, null);
		String password = RequestTool.get(request, passwordArg, null);
		
		if(StringTool.isEmpty(username)){
			throw new InvalidCredentialsException("no username specified");
		}
		
		User user = UserDao.getUserWithEmail(username);
		
		if(user==null){
			throw new UsernameNotFoundException();
		} else if(StringTool.isEmpty(password) ||
				!UserPasswordEncoder.isPasswordValid(user.getPassword(), password, user.getEmail())){
			throw new IncorrectPasswordException("invalid password for user:"+user.getEmail());
		}
		
		user.setLastLoginDate(new Date());
		user.setLastLoginIp(ServletUtils.getIpAddress(request));
		
		userRouter.user.put(user, null);
		
		DatarouterSession userSession = loginUser(user, request, response, 
											RememberMeCookieAuthenticator.isRememberMeCookieDesired(request));
		
		RobotDetector.get().iAmNotARobot(request);
		
		onSuccess(userSession);
		
		userSession.setUpdated(new Date());
		DatarouterSession.store(request, userSession);
		
		return userSession;
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
