package com.hotpads.handler.user.authenticate.okta;

public class OktaResponse{
	private static final String ACTIVE = "ACTIVE";
	private static final String MFA_REQUIRED = "MFA_REQUIRED";

	public String status;

	public Boolean isActive(){
		return ACTIVE.equals(status);
	}

	public Boolean isMfaRequired(){
		return MFA_REQUIRED.equals(status);
	}
}
