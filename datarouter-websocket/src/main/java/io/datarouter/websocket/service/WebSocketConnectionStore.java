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
package io.datarouter.websocket.service;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;
import javax.websocket.Session;

import io.datarouter.websocket.storage.session.WebSocketSession;
import io.datarouter.websocket.storage.session.WebSocketSessionKey;

@Singleton
public class WebSocketConnectionStore{

	private final Map<WebSocketSessionKey,WebSocketConnection> connections;

	public WebSocketConnectionStore(){
		this.connections = new ConcurrentHashMap<>();
	}

	public void put(WebSocketSession webSocketSession, Session session){
		connections.put(webSocketSession.getKey(), new WebSocketConnection(session, new Object()));
	}

	public Optional<WebSocketConnection> find(WebSocketSessionKey webSocketSessionKey){
		return Optional.ofNullable(connections.get(webSocketSessionKey));
	}

	public WebSocketConnection remove(WebSocketSessionKey webSocketSessionKey){
		return connections.remove(webSocketSessionKey);
	}

	public Set<Entry<WebSocketSessionKey,WebSocketConnection>> list(){
		return connections.entrySet();
	}

	public static class WebSocketConnection{
		public final Session session;
		public final Object lock;

		public WebSocketConnection(Session session, Object lock){
			this.session = session;
			this.lock = lock;
		}
	}

}
