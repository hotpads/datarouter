package com.hotpads.handler.user.authenticate.okta;

import java.util.Date;

public class OktaSessionResponse extends OktaResponse{
	public String userId;
	public String login;
	public Date createdAt;
	public Date expiresAt;
}
