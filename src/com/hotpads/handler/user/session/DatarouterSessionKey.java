package com.hotpads.handler.user.session;

import javax.persistence.Embeddable;

import com.hotpads.handler.user.session.BaseDatarouterSessionDatabeanKey;

@SuppressWarnings("serial")
@Embeddable
public class DatarouterSessionKey extends BaseDatarouterSessionDatabeanKey<DatarouterSessionKey>{
	public DatarouterSessionKey(){
	}
	
	public DatarouterSessionKey(String sessionToken) {
		super(sessionToken);
	}
}
