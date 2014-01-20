package com.hotpads.handler.user.authenticate.authenticator;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.user.DatarouterUserNodes;
import com.hotpads.handler.user.authenticate.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.handler.user.session.DatarouterSessionKey;
import com.hotpads.handler.user.session.DatarouterSessionTool;
import com.hotpads.util.core.DateTool;

public class DatarouterSessionAuthenticator extends BaseDatarouterAuthenticator{
	
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
		String sessionToken = DatarouterSessionTool.getSessionTokenFromCookie(request);
		DatarouterSession session = userNodes.getSessionNode().get(new DatarouterSessionKey(sessionToken), null);
		if(session == null){
			return null;
		}
		long msSinceLastAccess = System.currentTimeMillis() - session.getUpdated().getTime();
		if(msSinceLastAccess > SESSION_TIMOUT_MS){
			return null;
		}
		
		session.setUpdated(new Date());
		
		//TODO may want to always save to memcached, but less frequently to database
		userNodes.getSessionNode().put(session, null);
		
		return session;
		
	}
}
