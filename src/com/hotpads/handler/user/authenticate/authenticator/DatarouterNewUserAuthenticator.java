package com.hotpads.handler.user.authenticate.authenticator;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hotpads.handler.user.authenticate.BaseDatarouterAuthenticator;
import com.hotpads.handler.user.authenticate.DatarouterUserRole;
import com.hotpads.handler.user.authenticate.RequestAuthentication;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

public class DatarouterNewUserAuthenticator extends BaseDatarouterAuthenticator{

	public DatarouterNewUserAuthenticator(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}
	
	@Override
	public RequestAuthentication authenticate(){
		String email = null;//we don't know it
		List<DatarouterUserRole> roles = ListTool.wrap(DatarouterUserRole.anonymous);
		RequestAuthentication authentication = new RequestAuthentication(email, roles);
		
		userSession.setAnonUser(true);
		userSession.setUserRoles(SetTool.create(UserRole.ROLE_ANONYMOUS));
		userSession.setUserCreationDate(new Date());

		String newUserToken = UserSessionTokenTool.buildUserToken();
		userSession.setUserToken(newUserToken);
		UserSessionTokenTool.addUserTokenCookie(response, userSession.getUserToken());
		
		String newSessionToken = UserSessionTokenTool.buildSessionToken();
		userSession.setSessionToken(newSessionToken);
		UserSessionTokenTool.addSessionTokenCookie(response, userSession.getSessionToken());
		
		userSession.setUpdated(new Date());
		DatarouterSession.store(request, userSession);

		onSuccess(userSession);
		
		return userSession;
	}
}
