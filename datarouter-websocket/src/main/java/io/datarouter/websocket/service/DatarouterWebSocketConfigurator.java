/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.websocket.service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.inject.DatarouterInjector;
import io.datarouter.web.inject.InjectorRetriever;
import io.datarouter.websocket.auth.WebSocketAuthenticationFilter;

public abstract class DatarouterWebSocketConfigurator extends Configurator implements InjectorRetriever{
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
	public <T>T getEndpointInstance(Class<T> endpointClass){
		return injector.getInstance(endpointClass);
	}

}
