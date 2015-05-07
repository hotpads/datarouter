package com.hotpads.websocket.endpoint;

import javax.inject.Inject;
import javax.websocket.MessageHandler;

import com.hotpads.websocket.endpoint.SwedishEchoMessageHandler.SwedishEchoMessageHandlerFactory;

/**
 * return twice the message received
 * @author cguillaume
 */
public class SwedishEchoEndpoint extends BaseEndpoint{

	@Inject
	private SwedishEchoMessageHandlerFactory swedishEchoMessageHandlerFactory;

	@Override
	protected MessageHandler getMessageHandler(String userToken){
		return swedishEchoMessageHandlerFactory.create(userToken);
	}

}
