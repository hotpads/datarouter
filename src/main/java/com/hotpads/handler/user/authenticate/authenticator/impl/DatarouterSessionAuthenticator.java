package com.hotpads.handler.user.authenticate.authenticator.impl;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.authenticator.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.user.session.DatarouterSessionKey;
import com.hotpads.handler.user.session.DatarouterSessionManager;
import com.hotpads.util.core.DateTool;
import com.hotpads.util.core.ObjectTool;

public class DatarouterSessionAuthenticator extends BaseDatarouterAuthenticator{
	private static Logger logger = Logger.getLogger(DatarouterSessionAuthenticator.class);
	
	public static final Long
		SESSION_TIMOUT_MS = 30L * DateTool.MILLISECONDS_IN_MINUTE;
			
	private DatarouterUserNodes userNodes;
	
	public DatarouterSessionAuthenticator(HttpServletRequest request, HttpServletResponse response,
			DatarouterUserNodes userNodes) {
		super(request, response);
		this.userNodes = userNodes;
	}

	@Override
	public DatarouterSession getSession(){		
		String sessionToken = DatarouterSessionManager.getSessionTokenFromCookie(request);
		DatarouterSession session = userNodes.getSessionNode().get(new DatarouterSessionKey(sessionToken), null);
		if(session == null){
			return null;
		}
		long msSinceLastAccess = System.currentTimeMillis() - session.getUpdated().getTime();
		if(msSinceLastAccess > SESSION_TIMOUT_MS){
			return null;
		}
		
		//verify session's userToken matches cookie userToken.  if not, delete session to be safe
		String cookieUserToken = DatarouterSessionManager.getUserTokenFromCookie(request);
		if(ObjectTool.notEquals(cookieUserToken, session.getUserToken())){
			logger.warn("session userToken "+session.getUserToken()+" != cookie userToken "+cookieUserToken
					+", deleting session");
			userNodes.getSessionNode().delete(session.getKey(), null);
			DatarouterSessionManager.clearSessionTokenCookie(response);
			return null;
		}
		
		session.setUpdated(new Date());		
		return session;
		
	}
}