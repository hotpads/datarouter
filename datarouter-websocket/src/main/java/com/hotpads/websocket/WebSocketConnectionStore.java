package com.hotpads.websocket;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;
import javax.websocket.Session;

import com.hotpads.websocket.session.WebSocketSession;
import com.hotpads.websocket.session.WebSocketSessionKey;

@Singleton
public class WebSocketConnectionStore{

	private final Map<WebSocketSessionKey,Session> map;

	public WebSocketConnectionStore(){
		this.map = new ConcurrentHashMap<>();
	}

	public void put(WebSocketSession webSocketSession, Session session){
		map.put(webSocketSession.getKey(), session);
	}

	public Session get(WebSocketSessionKey webSocketSessionKey){
		return map.get(webSocketSessionKey);
	}

	public Session remove(WebSocketSessionKey webSocketSessionKey){
		return map.remove(webSocketSessionKey);
	}

	public Set<Entry<WebSocketSessionKey,Session>> list(){
		return map.entrySet();
	}

}
