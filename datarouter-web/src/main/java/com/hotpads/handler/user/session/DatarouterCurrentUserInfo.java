package com.hotpads.handler.user.session;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.DatarouterUser.DatarouterUserByUserTokenLookup;
import com.hotpads.handler.user.DatarouterUserNodes;

@Singleton
public class DatarouterCurrentUserInfo implements CurrentUserInfo{

	@Inject
	private DatarouterSessionManager sessionManager;
	@Inject
	private DatarouterUserNodes userNodes;

	@Override
	public String getEmail(HttpServletRequest request){
		String userToken = sessionManager.getUserTokenFromCookie(request);
		DatarouterUser user = userNodes.getUserNode().lookupUnique(new DatarouterUserByUserTokenLookup(userToken), null);
		return user.getUsername();
	}

}
