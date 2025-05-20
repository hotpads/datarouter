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
package io.datarouter.websocket.session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.trace.TraceSpanGroupType;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.websocket.WebSocketCounters;
import io.datarouter.websocket.config.DatarouterWebSocketSettingRoot;
import io.datarouter.websocket.service.WebSocketConnectionStore;
import io.datarouter.websocket.service.WebSocketConnectionStore.WebSocketConnection;
import io.datarouter.websocket.storage.session.WebSocketSessionKey;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class WebSocketApiService{
	private static final Logger logger = LoggerFactory.getLogger(WebSocketApiService.class);

	@Inject
	private WebSocketConnectionStore webSocketConnectionStore;
	@Inject
	private DatarouterWebSocketSettingRoot datarouterWebsocketSettingRoot;

	public boolean push(WebSocketCommandDto webSocketCommand) throws IOException{
		WebSocketSessionKey webSocketSessionKey = webSocketCommand.webSocketSessionKey();
		Optional<WebSocketConnection> connection = webSocketConnectionStore.find(webSocketSessionKey);
		if(connection.isEmpty()){
			logger.error("can not send message to unknown websocket session={}", webSocketSessionKey);
			return false;
		}
		logger.info("sending message session={}", webSocketSessionKey);
		WebSocketCounters.inc("sendText");
		Basic basicRemote = connection.get().session().getBasicRemote();
		// Synchronize on the connection because sendText is not thread-safe
		synchronized(connection.get().lock()){
			try(var _ = TracerTool.startSpan("websocket sendText", TraceSpanGroupType.HTTP)){
				String message = webSocketCommand.message();
				TracerTool.appendToSpanInfo("characters", message.length());
				basicRemote.sendText(message);
			}
		}
		return true;
	}

	public boolean isAlive(WebSocketCommandDto webSocketCommand){
		WebSocketSessionKey webSocketSessionKey = webSocketCommand.webSocketSessionKey();
		Optional<WebSocketConnection> connection = webSocketConnectionStore.find(webSocketSessionKey);
		if(connection.isEmpty()){
			return false;
		}
		Session session = connection.get().session();
		if(!session.isOpen()){
			// should I remove the session from the webSocketConnectionStore and invoke the onClose
			logger.warn("websocket already closed session={}", webSocketSessionKey);
			WebSocketCounters.inc("alreadyClosed");
			return false;
		}
		if(!datarouterWebsocketSettingRoot.testConnectionWithPing.get()){
			return true;
		}
		logger.info("sending ping session={}", webSocketSessionKey);
		WebSocketCounters.inc("sendPing");
		// Synchronize on the connection
		try{
			synchronized(connection.get().lock()){
				try(var _ = TracerTool.startSpan("websocket sendPing", TraceSpanGroupType.HTTP)){
					session.getBasicRemote().sendPing(ByteBuffer.allocate(0));
				}
			}
		}catch(Exception e){
			logger.warn("detected broken connection session={}", webSocketSessionKey, e);
			return false;
		}
		return true;
	}

}
