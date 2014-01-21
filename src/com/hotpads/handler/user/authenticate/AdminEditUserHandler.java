package com.hotpads.handler.user.authenticate;

import javax.inject.Inject;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;

public class AdminEditUserHandler extends BaseHandler{
	
	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;

	@Override
	protected Mav handleDefault(){
		return editUserForm();
	}
	
	@Handler
	private Mav editUserForm(){
		Mav mav = new Mav("/jsp/generic/adminEditUserForm.jsp");
		mav.put("authenticationConfig", authenticationConfig);
		return mav;
	}

	@Handler
	private Mav createUser(){
		Mav mav = new Mav("/jsp/generic/adminEditUserForm.jsp");
		mav.put("authenticationConfig", authenticationConfig);
		return mav;
	}
	
}
