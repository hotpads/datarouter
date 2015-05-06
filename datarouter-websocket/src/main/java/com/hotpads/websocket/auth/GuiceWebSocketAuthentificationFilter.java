package com.hotpads.websocket.auth;

import javax.inject.Inject;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

public class GuiceWebSocketAuthentificationFilter extends WebSocketAuthentificationFilter{

	@Inject
	private UserTokenRetriever userTokenRetriever;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException{}

	@Override
	protected UserTokenRetriever getUserTokenRetriever(){
		return userTokenRetriever;
	}

}
