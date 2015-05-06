package com.hotpads.websocket.config;

import java.util.HashSet;
import java.util.Set;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.websocket.endpoint.SwedishEchoEndpoint;
import com.hotpads.websocket.endpoint.WebSocketServicesEndpoint;

public abstract class WebSocketConfig implements ServerApplicationConfig{
	private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

	public static final String
		WEBSOCKET_URI_PREFIX = "ws",
		EVERYTHING_BUT_NOT_WEBSOCKET = "(?!/" + WebSocketConfig.WEBSOCKET_URI_PREFIX + ").*"
		;

	private final DatarouterWebSocketConfigurator webSocketConfigurator;

	public WebSocketConfig(DatarouterWebSocketConfigurator webSocketConfigurator){
		this.webSocketConfigurator = webSocketConfigurator;
	}

	@Override
	public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> endpointClasses){
		logger.warn("Initializing websocket endpoint");
		Set<ServerEndpointConfig> result = new HashSet<>();
		ServerEndpointConfig echoEndpointConfig = Builder
				.create(SwedishEchoEndpoint.class, "/" + WEBSOCKET_URI_PREFIX + "echo")
				.configurator(webSocketConfigurator)
				.build();
		result.add(echoEndpointConfig);
		ServerEndpointConfig servicesEndpointConfig = Builder
				.create(WebSocketServicesEndpoint.class, "/" + WEBSOCKET_URI_PREFIX + "services")
				.configurator(webSocketConfigurator)
				.build();
		result.add(servicesEndpointConfig);
		return result;
	}

	@Override
	public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned){
		return null;
	}

}
