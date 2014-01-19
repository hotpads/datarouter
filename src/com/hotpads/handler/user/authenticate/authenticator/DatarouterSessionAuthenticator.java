package com.hotpads.handler.user.authenticate.authenticator;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hotpads.databean.property.user.UserSession;
import com.hotpads.databean.property.user.util.UserSessionTokenTool;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.session.SessionDao;
import com.hotpads.util.core.DateTool;
import com.hotpads.websupport.authentication.BaseAuthenticator;
import com.hotpads.websupport.user.UserTool;

public class DatarouterSessionAuthenticator extends BaseAuthenticator{
	
	public static final Long
		//consider a user dead if 30 minutes has passed without a heartbeat
		SESSION_LIFESPAN_MS = 30L * DateTool.MILLISECONDS_IN_MINUTE;
			
	public DatarouterSessionAuthenticator(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public DatarouterSession getUserSession(){		
		String sessionToken = UserTool.getSessionToken(request);
		DatarouterSession userSession = SessionDao.getUserSession(sessionToken);
		if(userSession == null || System.currentTimeMillis() - userSession.getUpdated().getTime() > SESSION_LIFESPAN_MS){
			return null;
		}
		//userSession = SessionDao.getUserSession(sessionToken);
		//if(userSession==null){ return null; }
		
		user_token_override(request, userSession);
		
		onSuccess(userSession);
		SessionDao.saveUserSession(userSession);
		
		return userSession;
		
	}
	
	protected void user_token_override(HttpServletRequest request, DatarouterSession userSession) {
		String ut_override = RequestTool.get(request, UserSessionTokenTool.USER_TOKEN_OVERRIDE, null);
		if(UserSessionTokenTool.isValidUserToken(ut_override)){ 
			ut_override = UserSessionTokenTool.cleanUserTokenOverride(ut_override);
			userSession.setUserToken(ut_override);
			UserSessionTokenTool.addUserTokenCookie(response, userSession.getUserToken());
		}
	}
}
