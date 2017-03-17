package com.hotpads.websocket.endpoint;

import com.hotpads.websocket.session.WebSocketSession;

public interface WebSocketService{

	String getName();

	void onMessage(WebSocketSession webSocketSession, String message);

	default void onClose(){}

}
