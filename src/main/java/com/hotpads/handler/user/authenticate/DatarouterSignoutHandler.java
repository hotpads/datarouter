package com.hotpads.handler.user.authenticate;

import javax.inject.Inject;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.session.DatarouterSessionManager;

public class DatarouterSignoutHandler extends BaseHandler{
	
	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;

	@Override
	protected Mav handleDefault(){
		DatarouterSessionManager.clearSessionTokenCookie(response);
		DatarouterSessionManager.clearUserTokenCookie(response);
		return new Mav(Mav.REDIRECT+request.getContextPath());
	}
}
