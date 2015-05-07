package com.hotpads.websocket.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.hotpads.websocket.WebSocketTool;

public abstract class WebSocketAuthenticationFilter implements Filter{

	public static final String WEB_SOCKET_TOKEN = "WebSocketToken";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException{
		HttpServletRequest req = (HttpServletRequest)request;
		if(WebSocketTool.isHandShakeRequest(req)){
			String retrieveUserToken = getUserTokenRetriever().retrieveUserToken(req);
			HttpSession httpSession = req.getSession();
			httpSession.setAttribute(WEB_SOCKET_TOKEN, retrieveUserToken);
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy(){}

	protected abstract UserTokenRetriever getUserTokenRetriever();

}
