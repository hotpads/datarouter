package com.hotpads.handler.user.session;

import javax.persistence.Embeddable;

@SuppressWarnings("serial")
@Embeddable
public class AuthenticationTargetUrlKey extends BaseUserSessionDatabeanKey<AuthenticationTargetUrlKey>{
	public AuthenticationTargetUrlKey(){
	}
	
	public AuthenticationTargetUrlKey(String sessionToken) {
		super(sessionToken);
	}
}
