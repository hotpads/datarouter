package com.hotpads.websocket.endpoint;

import javax.inject.Inject;
import javax.websocket.MessageHandler;

import com.hotpads.websocket.endpoint.SwedishEchoMessageHandler.SwedishEchoMessageHandlerFactory;
import com.hotpads.websocket.session.WebSocketSession;

/**
 * return twice the message received
 * @author cguillaume
 */
public class SwedishEchoEndpoint extends BaseEndpoint{

	@Inject
	private SwedishEchoMessageHandlerFactory swedishEchoMessageHandlerFactory;

	@Override
	protected MessageHandler getMessageHandler(WebSocketSession webSocketSession){
		return swedishEchoMessageHandlerFactory.create(webSocketSession.getKey().getUserToken());
	}

}
