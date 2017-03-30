package com.hotpads.websocket.endpoint;

import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.handler.exception.ExceptionRecorder;
import com.hotpads.websocket.ServerAddressProvider;
import com.hotpads.websocket.WebSocketConnectionStore;
import com.hotpads.websocket.auth.WebSocketAuthenticationFilter;
import com.hotpads.websocket.session.PushService;
import com.hotpads.websocket.session.WebSocketSession;

public abstract class BaseEndpoint extends Endpoint{
	private static final Logger logger = LoggerFactory.getLogger(BaseEndpoint.class);

	@Inject
	private PushService pushService;
	@Inject
	private ServerAddressProvider serverAddressProvider;
	@Inject
	private WebSocketConnectionStore webSocketConnectionStore;
	@Inject
	private ExceptionRecorder exceptionRecorder;

	protected WebSocketSession webSocketSession;
	private ClosableMessageHandler messageHandler;

	@Override
	public void onOpen(Session session, EndpointConfig endpointConfig){
		String userToken = (String)endpointConfig.getUserProperties().get(
				WebSocketAuthenticationFilter.WEB_SOCKET_TOKEN);
		String serverAddress = serverAddressProvider.get();
		webSocketSession = new WebSocketSession(userToken, serverAddress);
		pushService.register(webSocketSession);
		webSocketConnectionStore.put(webSocketSession, session);
		messageHandler = getMessageHandler();
		session.addMessageHandler(messageHandler);
	}

	protected abstract ClosableMessageHandler getMessageHandler();

	@Override
	public void onClose(Session session, CloseReason closeReason){ // TODO handle exception
		logger.info("Closing websocket session {} because {}", webSocketSession, closeReason);
		messageHandler.onClose();
		pushService.unregister(webSocketSession.getKey());
		webSocketConnectionStore.remove(webSocketSession.getKey());
	}

	@Override
	public void onError(Session session, Throwable thr){
		logger.error("Error on websocket session {}", webSocketSession, thr);
		exceptionRecorder.tryRecordException(thr, getClass().getName());
	}

}
