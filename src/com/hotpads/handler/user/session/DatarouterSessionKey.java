package com.hotpads.handler.user.session;

import javax.persistence.Embeddable;

import com.hotpads.handler.user.session.BaseUserSessionDatabeanKey;

@SuppressWarnings("serial")
@Embeddable
public class DatarouterSessionKey extends BaseUserSessionDatabeanKey<DatarouterSessionKey>{
	public DatarouterSessionKey(){
	}
	
	public DatarouterSessionKey(String sessionToken) {
		super(sessionToken);
	}
}
