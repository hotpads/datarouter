package com.hotpads.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;
import javax.websocket.Session;

import com.hotpads.websocket.session.WebSocketSession;
import com.hotpads.websocket.session.WebSocketSessionKey;

@Singleton
public class WebSocketConnectionStore{

	private final Map<WebSocketSessionKey,Session> map;

	public WebSocketConnectionStore(){
		map = new ConcurrentHashMap<>();
	}

	public void put(WebSocketSession webSocketSession, Session session){
		map.put(webSocketSession.getKey(), session);
	}

	public Session get(WebSocketSessionKey webSocketSessionKey){
		return map.get(webSocketSessionKey);
	}

}
