package com.hotpads.websocket.endpoint;

import javax.websocket.MessageHandler;

public interface ClosableMessageHandler extends MessageHandler{

	default void onClose(){}

}
