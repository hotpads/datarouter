package com.hotpads.websocket.config;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.DatarouterInjector;
import com.hotpads.websocket.auth.WebSocketAuthenticationFilter;

public abstract class DatarouterWebSocketConfigurator extends Configurator{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterWebSocketConfigurator.class);

	private DatarouterInjector injector;

	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response){
		logger.info("WebSocket handshake intersepted");
		HttpSession httpSession = (HttpSession)request.getHttpSession();
		sec.getUserProperties().put(WebSocketAuthenticationFilter.WEB_SOCKET_TOKEN,
				httpSession.getAttribute(WebSocketAuthenticationFilter.WEB_SOCKET_TOKEN));
		if(injector != null){
			return;
		}
		logger.info("Exctracking injector from HandshakeRequest");
		ServletContext servletContext = httpSession.getServletContext();
		injector = getInjector(servletContext);
	}

	@Override
	public <T>T getEndpointInstance(Class<T> endpointClass) throws InstantiationException{
		return injector.getInstance(endpointClass);
	}

	protected abstract DatarouterInjector getInjector(ServletContext servletContext);

}
