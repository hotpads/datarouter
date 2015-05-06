package com.hotpads.websocket.endpoint;

import java.util.HashMap;
import java.util.Map;

import com.hotpads.DatarouterInjector;

public abstract class WebSocketServices{

	private final DatarouterInjector injector;

	private final Map<String, Class<? extends WebSocketService>> services;

	public WebSocketServices(DatarouterInjector injector){
		this.injector = injector;
		this.services = new HashMap<>();
	}

	protected void registerService(Class<? extends WebSocketService> clazz){
		WebSocketService service = injector.getInstance(clazz);
		services.put(service.getName(), clazz);
	}

	public WebSocketService getNewInstance(String serviceName){
		return injector.getInstance(services.get(serviceName));
	}

}
