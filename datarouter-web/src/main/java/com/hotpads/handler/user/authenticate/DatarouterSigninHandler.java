package com.hotpads.handler.user.authenticate;

import javax.inject.Inject;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.handler.user.authenticate.config.DatarouterAuthenticationConfig;
import com.hotpads.handler.user.authenticate.okta.OktaSettings;

public class DatarouterSigninHandler extends BaseHandler{

	@Inject
	private DatarouterAuthenticationConfig authenticationConfig;

	@Inject
	private OktaSettings oktaSettings;

	@Handler(defaultHandler = true)
	protected Mav showForm(){
		Mav mav = new Mav("/jsp/authentication/signinForm.jsp");
		mav.put("authenticationConfig", authenticationConfig);
		mav.put("allowOkta", oktaSettings.getShouldProcess());
		mav.put("requireOkta", oktaSettings.getIsOktaRequired().getValue());
		mav.put("orgUrl", oktaSettings.getOrgUrl().getValue());
		mav.put("redirectUrl", request.getRequestURL());

		return mav;
	}
}
