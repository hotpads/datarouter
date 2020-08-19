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
package io.datarouter.websocket.session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import javax.inject.Inject;
import javax.websocket.RemoteEndpoint.Basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.types.Param;
import io.datarouter.websocket.WebSocketCounters;
import io.datarouter.websocket.config.DatarouterWebSocketSettingRoot;
import io.datarouter.websocket.service.WebSocketConnectionStore;
import io.datarouter.websocket.service.WebSocketConnectionStore.WebSocketConnection;

public class WebSocketApiHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(WebSocketApiHandler.class);

//	private static final String DTO_NAME = new DatarouterHttpClientDefaultConfig().getDtoParameterName();
	private static final String DTO_NAME = "dataTransferObject";

	@Inject
	private WebSocketConnectionStore webSocketConnectionStore;
	@Inject
	private DatarouterWebSocketSettingRoot datarouterWebsocketSettingRoot;

	/**
	 * same name as WebSocketCommandName.PUSH.getPath()
	 */
	@Handler
	private boolean push(@Param(DTO_NAME) WebSocketCommand webSocketCommand) throws IOException{
		Optional<WebSocketConnection> connection = webSocketConnectionStore.find(webSocketCommand
				.getWebSocketSessionKey());
		if(connection.isEmpty()){
			logger.error("can not send message to unknown websocket session={}", webSocketCommand
					.getWebSocketSessionKey());
			return false;
		}
		logger.info("sending message session={}", webSocketCommand.getWebSocketSessionKey());
		WebSocketCounters.inc("sendText");
		Basic basicRemote = connection.get().session.getBasicRemote();
		// Synchronize on the connection because sendText is not thread-safe
		synchronized(connection.get().lock){
			basicRemote.sendText(webSocketCommand.getMessage());
		}
		return true;
	}

	/**
	 * same name as WebSocketCommandName.IS_ALIVE.getPath()
	 */
	@Handler
	private boolean isAlive(@Param(DTO_NAME) WebSocketCommand webSocketCommand){
		Optional<WebSocketConnection> connection = webSocketConnectionStore.find(webSocketCommand
				.getWebSocketSessionKey());
		if(connection.isEmpty()){
			return false;
		}
		if(!datarouterWebsocketSettingRoot.testConnectionWithPing.get()){
			return true;
		}
		logger.info("sending ping session={}", webSocketCommand.getWebSocketSessionKey());
		WebSocketCounters.inc("sendPing");
		Basic basicRemote = connection.get().session.getBasicRemote();
		// Synchronize on the connection
		try{
			synchronized(connection.get().lock){
				basicRemote.sendPing(ByteBuffer.allocate(0));
			}
		}catch(Exception e){
			logger.info("detected broken connection session={}", webSocketCommand.getWebSocketSessionKey(), e);
			return false;
		}
		return true;
	}

}