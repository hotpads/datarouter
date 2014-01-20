package com.hotpads.handler.user.authenticate;

import com.hotpads.handler.user.session.DatarouterSession;

public interface DatarouterAuthenticator{

	DatarouterSession getSession();
	
}