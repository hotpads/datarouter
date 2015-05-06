package com.hotpads.websocket.session;

import com.hotpads.DatarouterInjector;
import com.hotpads.handler.BaseDispatcher;
import com.hotpads.handler.dispatcher.DatarouterDispatcher;

public class WebSocketApiDispatcher extends BaseDispatcher{

	public static final String WEBSOCKET_COMMAND = "/websocketCommand";

	public WebSocketApiDispatcher(DatarouterInjector injector, String servletContextPath, String urlPrefix){
		super(injector, servletContextPath, urlPrefix);

		handle(WEBSOCKET_COMMAND + DatarouterDispatcher.ANYTHING).withHandler(WebSocketApiHandler.class);
	}

}
