package com.hotpads.websocket.endpoint;

import javax.inject.Inject;
import javax.websocket.MessageHandler;

import com.hotpads.websocket.endpoint.WebSocketServicesMessageHandler.WebSocketServicesMessageHandlerFactory;
import com.hotpads.websocket.session.WebSocketSession;

public class WebSocketServicesEndpoint extends BaseEndpoint{

	@Inject
	private WebSocketServicesMessageHandlerFactory webSocketServicesMessageHandlerFactory;

	@Override
	protected MessageHandler getMessageHandler(WebSocketSession webSocketSession){
		return webSocketServicesMessageHandlerFactory.create(webSocketSession);
	}

}
