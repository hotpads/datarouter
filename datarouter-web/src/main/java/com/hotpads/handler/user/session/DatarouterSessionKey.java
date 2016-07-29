package com.hotpads.handler.user.session;

import javax.persistence.Embeddable;

@SuppressWarnings("serial")
@Embeddable
public class DatarouterSessionKey extends BaseDatarouterSessionDatabeanKey<DatarouterSessionKey>{
	DatarouterSessionKey(){
	}

	public DatarouterSessionKey(String sessionToken) {
		super(sessionToken);
	}
}
