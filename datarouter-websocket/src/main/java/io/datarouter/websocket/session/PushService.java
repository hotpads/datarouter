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

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.httpclient.request.DatarouterHttpRequest;
import io.datarouter.httpclient.request.DatarouterHttpRequest.HttpRequestMethod;
import io.datarouter.httpclient.response.DatarouterHttpResponse;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.util.KeyRangeTool;
import io.datarouter.websocket.WebSocketCounters;
import io.datarouter.websocket.config.DatarouterWebSocketPaths;
import io.datarouter.websocket.endpoint.WebSocketServices;
import io.datarouter.websocket.storage.session.DatarouterWebSocketSessionDao;
import io.datarouter.websocket.storage.session.WebSocketSession;
import io.datarouter.websocket.storage.session.WebSocketSessionKey;
import io.datarouter.websocket.storage.subscription.DatarouterWebSocketSubscriptionDao;
import io.datarouter.websocket.storage.subscription.WebSocketSubscriptionKey;

@Singleton
public class PushService{
	private static final Logger logger = LoggerFactory.getLogger(PushService.class);

	@Inject
	private PushServiceHttpClient httpClient;
	@Inject
	private DatarouterWebSocketSessionDao webSocketDao;
	@Inject
	private DatarouterWebSocketSubscriptionDao webSocketSubscriptionDao;
	@Inject
	private WebSocketServices webSocketServices;
	@Inject
	private DatarouterWebSocketPaths paths;

	public void register(WebSocketSession webSocketSession){
		webSocketDao.put(webSocketSession);
	}

	public void unregister(WebSocketSessionKey webSocketSessionKey){
		webSocketDao.delete(webSocketSessionKey);
	}

	public long getNumberOfSession(String userToken){
		WebSocketSessionKey prefix = new WebSocketSessionKey(userToken, null);
		return webSocketDao.count(KeyRangeTool.forPrefix(prefix));
	}

	public void forwardToAll(String userToken, String message){
		WebSocketSessionKey prefix = new WebSocketSessionKey(userToken, null);
		webSocketDao.scanWithPrefix(prefix)
				.forEach(session -> forward(session, message));
	}

	public void forwardToTopic(String topic, String message){
		webSocketSubscriptionDao.scanKeysWithPrefix(new WebSocketSubscriptionKey(topic, null, null))
				.map(key -> new WebSocketSessionKey(key.getUserToken(), key.getWebSocketSessionId()))
				.batch(100)
				.map(webSocketDao::getMulti)
				.concatenate(Scanner::of)
				.forEach(session -> forward(session, message));
	}

	public boolean forward(String userToken, Long sessionId, String message){
		Optional<WebSocketSession> session = webSocketDao.find(new WebSocketSessionKey(userToken, sessionId));
		if(session.isEmpty()){
			throw new RuntimeException("WebSocket session not found");
		}
		return forward(session.get(), message);
	}

	public boolean forward(WebSocketSession webSocketSession, String message){
		DatarouterHttpResponse response = executeCommand(paths.websocketCommand.push, webSocketSession, message);
		boolean success = Boolean.parseBoolean(response.getEntity());
		if(!success){
			logger.error("Forwarding to {} failed: deleting the session", webSocketSession);
			unregister(webSocketSession.getKey());
			webSocketServices.listSampleInstances().forEach(service -> service.onSessionVacuum(webSocketSession
					.getKey()));
		}
		return success;
	}

	public boolean isAlive(WebSocketSession webSocketSession){
		DatarouterHttpResponse response = executeCommand(paths.websocketCommand.isAlive, webSocketSession, null);
		return Boolean.parseBoolean(response.getEntity());
	}

	// TODO Optimization: don't do the http call if the socket is open on the current server
	private DatarouterHttpResponse executeCommand(PathNode path, WebSocketSession webSocketSession, String message){
		WebSocketCounters.inc("command " + path.getValue());
		String url = "http://" + webSocketSession.getServerName() + path.toSlashedString();
		DatarouterHttpRequest request = new DatarouterHttpRequest(HttpRequestMethod.POST, url, false);
		WebSocketCommand webSocketCommand = new WebSocketCommand(webSocketSession.getKey(), message);
		httpClient.addDtoToPayload(request, webSocketCommand, null);
		return httpClient.execute(request);
	}

}
