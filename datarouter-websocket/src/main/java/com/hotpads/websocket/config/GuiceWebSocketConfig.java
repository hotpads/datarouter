package com.hotpads.websocket.config;


public abstract class GuiceWebSocketConfig extends WebSocketConfig{

	public GuiceWebSocketConfig(){
		super(new GuiceDatarouterWebSocketConfigurator());
	}

}
