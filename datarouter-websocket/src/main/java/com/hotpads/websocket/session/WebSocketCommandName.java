package com.hotpads.websocket.session;

public enum WebSocketCommandName{
	PUSH("push"),
	IS_ALIVE("isAlive")
	;

	private final String path;

	WebSocketCommandName(String path){
		this.path = path;
	}

	public String getPath(){
		return path;
	}

}
