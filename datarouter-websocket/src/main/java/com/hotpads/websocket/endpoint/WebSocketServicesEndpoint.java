package com.hotpads.websocket.endpoint;

import javax.inject.Inject;

import com.hotpads.websocket.endpoint.WebSocketServicesMessageHandler.WebSocketServicesMessageHandlerFactory;

public class WebSocketServicesEndpoint extends BaseEndpoint{

	@Inject
	private WebSocketServicesMessageHandlerFactory webSocketServicesMessageHandlerFactory;

	@Override
	protected WebSocketServicesMessageHandler getMessageHandler(){
		return webSocketServicesMessageHandlerFactory.create(webSocketSession);
	}

}
