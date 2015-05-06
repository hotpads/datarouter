package com.hotpads.websocket.session;

public class WebSocketCommand{

	private WebSocketSessionKey webSocketSessionKey;
	private String message;

	public WebSocketCommand(WebSocketSessionKey webSocketSessionKey, String message){
		this.webSocketSessionKey = webSocketSessionKey;
		this.message = message;
	}

	public WebSocketSessionKey getWebSocketSessionKey(){
		return webSocketSessionKey;
	}

	public String getMessage(){
		return message;
	}

}
