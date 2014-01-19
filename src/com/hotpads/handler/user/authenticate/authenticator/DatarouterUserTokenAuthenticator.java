package com.hotpads.handler.user.authenticate.authenticator;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.databean.property.user.UserSession;
import com.hotpads.databean.property.user.security.UserRole;
import com.hotpads.databean.property.user.util.UserSessionTokenTool;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.StringTool;
import com.hotpads.websupport.authentication.BaseAuthenticator;

public class DatarouterUserTokenAuthenticator extends BaseAuthenticator{
	
	public DatarouterUserTokenAuthenticator(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public DatarouterSession getUserSession(){

		String cookieUserToken = UserSessionTokenTool.getUserTokenFromCookie(request);
		if( ! UserSessionTokenTool.isValidUserToken(cookieUserToken)){ cookieUserToken = null; }
		
		String userTokenOverride = RequestTool.get(request, UserSessionTokenTool.USER_TOKEN_OVERRIDE, null);
		if( ! UserSessionTokenTool.isValidUserToken(userTokenOverride)){ userTokenOverride = null; }
		userTokenOverride = UserSessionTokenTool.cleanUserTokenOverride(userTokenOverride);
		
		if(StringTool.isEmpty(cookieUserToken) && StringTool.isEmpty(userTokenOverride)){
			return null;
		}
		
		String cookieSessionToken = UserSessionTokenTool.getSessionTokenFromCookie(request);
		if( ! UserSessionTokenTool.isValidUserToken(cookieSessionToken)){ cookieSessionToken = null; }

		if(StringTool.isEmpty(cookieSessionToken)){
			cookieSessionToken =  UserSessionTokenTool.buildSessionToken();
			UserSessionTokenTool.addSessionTokenCookie(response, cookieSessionToken);
		}
		
		//valid token found, return a UserSession
		
		DatarouterSession userSession = new DatarouterSession();
		
		userSession.setAnonUser(true);
		userSession.setUserRoles(SetTool.create(UserRole.ROLE_ANONYMOUS));
		userSession.setUserToken(cookieUserToken);
		userSession.setUserCreationDate(new Date()); //experimentTODO how to get actual creation date?? 
		//maybe set to today-5days since session must have expired out of memcached by then - but could have lost memcached...
		
		if(StringTool.notEmpty(userTokenOverride)){
			userSession.setUserToken(userTokenOverride);
			UserSessionTokenTool.addUserTokenCookie(response, userSession.getUserToken());
		}
		userSession.setSessionToken(cookieSessionToken);
		userSession.setUpdated(new Date());
		DatarouterSession.store(request, userSession);
		
		onSuccess(userSession);
		return userSession;
	}
	
	
}
