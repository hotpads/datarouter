package com.hotpads.handler.user.session;


@SuppressWarnings("serial")
public class DatarouterSessionKey extends BaseDatarouterSessionDatabeanKey<DatarouterSessionKey>{
	DatarouterSessionKey(){
	}

	public DatarouterSessionKey(String sessionToken) {
		super(sessionToken);
	}
}
