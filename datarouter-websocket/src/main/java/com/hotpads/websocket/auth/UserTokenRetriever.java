package com.hotpads.websocket.auth;

import javax.servlet.http.HttpServletRequest;

public interface UserTokenRetriever{

	String retrieveUserToken(HttpServletRequest req);

}
