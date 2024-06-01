/*
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

import java.util.Set;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.websocket.config.DatarouterWebSocketPaths;
import io.datarouter.websocket.endpoint.SwedishEchoEndpoint;
import io.datarouter.websocket.endpoint.WebSocketServicesEndpoint;

public abstract class WebSocketConfig implements ServerApplicationConfig{
	private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

	public static final String
			WEBSOCKET_URI_PREFIX = "ws",
			EVERYTHING_BUT_NOT_WEBSOCKET = "(?!/" + WebSocketConfig.WEBSOCKET_URI_PREFIX + "/).*";

	private final DatarouterWebSocketConfigurator webSocketConfigurator;

	public WebSocketConfig(DatarouterWebSocketConfigurator webSocketConfigurator){
		this.webSocketConfigurator = webSocketConfigurator;
	}

	@Override
	public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> endpointClasses){
		logger.warn("Initializing websocket endpoint with {}", getClass().getSimpleName());
		DatarouterWebSocketPaths datarouterWebSocketPaths = new DatarouterWebSocketPaths();
		ServerEndpointConfig echoEndpointConfig = Builder
				.create(SwedishEchoEndpoint.class, datarouterWebSocketPaths.ws.echo.toSlashedString())
				.configurator(webSocketConfigurator)
				.build();
		ServerEndpointConfig servicesEndpointConfig = Builder
				.create(WebSocketServicesEndpoint.class, datarouterWebSocketPaths.ws.services.toSlashedString())
				.configurator(webSocketConfigurator)
				.build();
		return Set.of(echoEndpointConfig, servicesEndpointConfig);
	}

	@Override
	public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned){
		return null;
	}

}
