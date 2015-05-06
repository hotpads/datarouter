package com.hotpads.websocket.endpoint;

public interface WebSocketService{

	String getName();

	void onMessage(String userToken, String message);

}
