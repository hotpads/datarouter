package com.hotpads.websocket.endpoint;

import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import com.hotpads.websocket.ServerAddressProvider;
import com.hotpads.websocket.WebSocketConnectionStore;
import com.hotpads.websocket.auth.WebSocketAuthentificationFilter;
import com.hotpads.websocket.session.PushService;
import com.hotpads.websocket.session.WebSocketSession;

public abstract class BaseEndpoint extends Endpoint{

	@Inject
	private PushService pushService;
	@Inject
	private ServerAddressProvider serverAddressProvider;
	@Inject
	private WebSocketConnectionStore webSocketConnectionStore;

	private WebSocketSession webSocketSession;

	@Override
	public void onOpen(Session session, EndpointConfig endpointConfig){
		String userToken = (String)endpointConfig.getUserProperties().get(
				WebSocketAuthentificationFilter.WEB_SOCKET_TOKEN);
		String serverAddress = serverAddressProvider.get();
		webSocketSession = new WebSocketSession(userToken, serverAddress);
		pushService.register(webSocketSession);
		webSocketConnectionStore.put(webSocketSession, session);
		MessageHandler messageHandler = getMessageHandler(userToken);
		session.addMessageHandler(messageHandler);
	}

	protected abstract MessageHandler getMessageHandler(String userToken);

	@Override
	public void onClose(Session session, CloseReason closeReason) {
		pushService.unregister(webSocketSession.getKey());
	}

}
