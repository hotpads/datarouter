package com.hotpads.websocket.session;

import javax.inject.Inject;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.mav.Mav;
import com.hotpads.websocket.ServerAddressProvider;
import com.hotpads.websocket.WebSocketConnectionStore;

public class WebSocketToolHandler extends BaseHandler{

	public static final String PATH = "/websocketTool";

	@Inject
	private WebSocketConnectionStore webSocketConnectionStore;
	@Inject
	private WebSocketSessionNodeProvider webSocketSessionNodeProvider;
	@Inject
	private ServerAddressProvider serverAddressProvider;

	@Handler(defaultHandler = true)
	private Mav list(){
		Mav mav = new Mav("/jsp/websocketTool.jsp");
		mav.put("localStoreSize", webSocketConnectionStore.list().size());
		long centralizeMappingSize = webSocketSessionNodeProvider.get().stream(null, null)
				.filter(wsSession -> wsSession.getServerName().equals(serverAddressProvider.get()))
				.count();
		mav.put("centralizedMappingSize", centralizeMappingSize);
		return mav;
	}

}
