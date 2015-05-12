package com.hotpads.websocket.endpoint;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.MessageHandler.Whole;

import com.hotpads.websocket.session.PushService;

public class SwedishEchoMessageHandler implements Whole<String>{

	@Singleton
	public static class SwedishEchoMessageHandlerFactory{

		private final PushService pushService;

		@Inject
		public SwedishEchoMessageHandlerFactory(PushService pushService){
			this.pushService = pushService;
		}

		public SwedishEchoMessageHandler create(String userToken){
			return new SwedishEchoMessageHandler(pushService, userToken);
		}

	}

	private final PushService pushService;
	private final String userToken;

	public SwedishEchoMessageHandler(PushService pushService, String userToken){
		this.pushService = pushService;
		this.userToken = userToken;
	}

	@Override
	public void onMessage(String message){
		pushService.forwardToAll(userToken, message + " " + message);
	}

}
