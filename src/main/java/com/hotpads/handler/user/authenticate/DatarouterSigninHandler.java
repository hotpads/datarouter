package com.hotpads.handler.user.authenticate;

import javax.inject.Inject;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;

public class DatarouterSigninHandler extends BaseHandler{
	
	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;

	@Override
	protected Mav handleDefault() {
		Mav mav = new Mav("/jsp/authentication/signinForm.jsp");
		mav.put("authenticationConfig", authenticationConfig);
		return mav;
	}
	
}
