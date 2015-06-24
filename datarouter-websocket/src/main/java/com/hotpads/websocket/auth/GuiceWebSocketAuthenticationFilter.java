package com.hotpads.websocket.auth;

import javax.inject.Inject;
import javax.servlet.FilterConfig;

public class GuiceWebSocketAuthenticationFilter extends WebSocketAuthenticationFilter{

	@Inject
	private UserTokenRetriever userTokenRetriever;

	@Override
	public void init(FilterConfig filterConfig){}

	@Override
	protected UserTokenRetriever getUserTokenRetriever(){
		return userTokenRetriever;
	}

}
