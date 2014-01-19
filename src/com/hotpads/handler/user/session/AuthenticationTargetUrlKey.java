package com.hotpads.handler.user.session;

import javax.persistence.Embeddable;

@SuppressWarnings("serial")
@Embeddable
@Deprecated//use cookie
public class AuthenticationTargetUrlKey extends BaseDatarouterSessionDatabeanKey<AuthenticationTargetUrlKey>{
	public AuthenticationTargetUrlKey(){
	}
	
	public AuthenticationTargetUrlKey(String sessionToken) {
		super(sessionToken);
	}
}
