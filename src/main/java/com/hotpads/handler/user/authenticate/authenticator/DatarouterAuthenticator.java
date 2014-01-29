package com.hotpads.handler.user.authenticate.authenticator;

import com.hotpads.handler.user.session.DatarouterSession;

public interface DatarouterAuthenticator{

	DatarouterSession getSession();
	
}