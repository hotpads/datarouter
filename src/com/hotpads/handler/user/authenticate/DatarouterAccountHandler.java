package com.hotpads.handler.user.authenticate;

import javax.inject.Inject;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;

public class DatarouterAccountHandler extends BaseHandler{
	
	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;

	@Override
	protected Mav handleDefault(){
		Mav mav = new Mav("/jsp/generic/signinForm.jsp");
		mav.put("authenticationConfig", authenticationConfig);
		return mav;
	}
	
}
