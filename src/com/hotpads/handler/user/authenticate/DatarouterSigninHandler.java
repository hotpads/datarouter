package com.hotpads.handler.user.authenticate;

import java.util.Random;

import javax.inject.Inject;

import com.google.inject.Injector;
import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.util.core.number.RandomTool;

public class DatarouterSigninHandler extends BaseHandler{
	
	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;

	@Override
	protected Mav handleDefault(){
		Mav mav = new Mav("/jsp/generic/signinForm.jsp");
		mav.put("authenticationConfig", authenticationConfig);
		return mav;
	}
	
}
