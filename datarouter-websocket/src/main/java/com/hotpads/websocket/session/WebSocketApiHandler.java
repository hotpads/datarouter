package com.hotpads.websocket.session;

import java.io.IOException;

import javax.inject.Inject;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.handler.BaseHandler;
import com.hotpads.handler.encoder.JsonEncoder;
import com.hotpads.handler.types.P;
import com.hotpads.websocket.WebSocketConnectionStore;

public class WebSocketApiHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(WebSocketApiHandler.class);

//	private static final String DTO_NAME = new HotPadsHttpClientDefaultConfig().getDtoParameterName();
	private static final String DTO_NAME = "dataTransferObject";

	@Inject
	private WebSocketConnectionStore webSocketConnectionStore;

	/**
	 * same name as WebSocketCommandName.PUSH.getPath()
	 */
	@Handler(encoder=JsonEncoder.class)
	private boolean push(@P(DTO_NAME) WebSocketCommand webSocketCommand) throws IOException{
		Session session = webSocketConnectionStore.get(webSocketCommand.getWebSocketSessionKey());
		if(session == null) {
			logger.error("Trying to send a message to not anymore connected seession ({})", webSocketCommand
					.getWebSocketSessionKey());
			return false;
		}
		Basic basicRemote = session.getBasicRemote();
		basicRemote.sendText(webSocketCommand.getMessage());
		return true;
	}

	/**
	 * same name as WebSocketCommandName.IS_ALIVE.getPath()
	 */
	@Handler(encoder=JsonEncoder.class)
	private boolean isAlive(@P(DTO_NAME) WebSocketCommand webSocketCommand){
		Session session = webSocketConnectionStore.get(webSocketCommand.getWebSocketSessionKey());
		return session != null;
	}

}
