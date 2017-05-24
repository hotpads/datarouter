package com.hotpads.handler.user.authenticate.okta;

public class OktaSessionResponse{
	private static final String ACTIVE = "ACTIVE";

	public String login;
	public String status;

	public Boolean isActive(){
		return ACTIVE.equals(status);
	}
}
