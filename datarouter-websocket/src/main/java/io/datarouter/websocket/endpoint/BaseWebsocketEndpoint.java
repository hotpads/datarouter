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
package io.datarouter.websocket.endpoint;

import java.io.EOFException;
import java.util.Date;

import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.websocket.WebSocketCounters;
import io.datarouter.websocket.auth.WebSocketAuthenticationFilter;
import io.datarouter.websocket.service.ServerAddressProvider;
import io.datarouter.websocket.service.WebSocketConnectionStore;
import io.datarouter.websocket.session.PushService;
import io.datarouter.websocket.storage.session.WebSocketSession;

public abstract class BaseWebsocketEndpoint extends Endpoint{
	private static final Logger logger = LoggerFactory.getLogger(BaseWebsocketEndpoint.class);

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
		WebSocketCounters.inc("onOpen");
		String userToken = (String)endpointConfig.getUserProperties().get(
				WebSocketAuthenticationFilter.WEB_SOCKET_TOKEN);
		String serverAddress = serverAddressProvider.get();
		webSocketSession = new WebSocketSession(userToken, null, new Date(), serverAddress);
		pushService.register(webSocketSession);
		logger.info("Opening websocket session={}", webSocketSession);
		webSocketConnectionStore.put(webSocketSession, session);
		messageHandler = getMessageHandler();
		session.addMessageHandler(messageHandler);
	}

	protected abstract ClosableMessageHandler getMessageHandler();

	@Override
	public void onClose(Session session, CloseReason closeReason){ // TODO handle exception
		WebSocketCounters.inc("onClose");
		logger.info("Closing websocket session={} closeReason={}", webSocketSession, closeReason);
		messageHandler.onClose();
		pushService.unregister(webSocketSession.getKey());
		webSocketConnectionStore.remove(webSocketSession.getKey());
	}

	@Override
	public void onError(Session session, Throwable thr){
		// some bad closes throw an Exception, then the onCLose method get called
		if(thr instanceof EOFException){
			WebSocketCounters.inc("bad close");
			logger.info("Bad websocket closing session={}", webSocketSession, thr);
			return;
		}
		WebSocketCounters.inc("onError");
		logger.error("Error on websocket session={}", webSocketSession, thr);
		exceptionRecorder.tryRecordException(thr, getClass().getName());
	}

}
