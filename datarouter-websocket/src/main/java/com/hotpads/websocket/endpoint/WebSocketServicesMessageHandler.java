package com.hotpads.websocket.endpoint;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.MessageHandler.Whole;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.exception.ExceptionRecorder;

public class WebSocketServicesMessageHandler implements Whole<String>{
	private static final Logger logger = LoggerFactory.getLogger(WebSocketServicesMessageHandler.class);

	private final WebSocketServices services;
	private final ExceptionRecorder exceptionRecorder;

	private final String userToken;
	private final Map<String, WebSocketService> openedServices;

	@Singleton
	public static class WebSocketServicesMessageHandlerFactory{

		@Inject
		private WebSocketServices services;
		@Inject
		private ExceptionRecorder exceptionRecorder;

		public WebSocketServicesMessageHandler create(String userToken){
			return new WebSocketServicesMessageHandler(services, exceptionRecorder, userToken);
		}

	}

	private WebSocketServicesMessageHandler(WebSocketServices services, ExceptionRecorder exceptionRecorder, String userToken){
		this.services = services;
		this.exceptionRecorder = exceptionRecorder;
		this.userToken = userToken;
		this.openedServices = new HashMap<>();
	}

	@Override
	public void onMessage(String message){
		int prefixEndIndex = message.indexOf("|");
		if(prefixEndIndex == -1){
			handleError("No service");
			return;
		}
		String serviceString = message.substring(0, prefixEndIndex);
		if(DrStringTool.isEmpty(serviceString)){
			handleError("No service");
			return;
		}
		WebSocketService service = openedServices.get(serviceString);
		if(service == null){
			service = services.getNewInstance(serviceString);
		}
		if(service == null){
			handleError("No service found for \"" + serviceString + "\"");
			return;
		}
		openedServices.put(serviceString, service);
		String paypload = message.substring(prefixEndIndex + 1);
		try{
			service.onMessage(userToken, paypload);
		}catch(Exception exception){
			logger.warn("Exception in websocket service handling message", exception);
			exceptionRecorder.tryRecordException(exception, service.getClass().toString());
		}
	}

	private void handleError(String errorMessage){
		Exception exception = new Exception(errorMessage);
		logger.warn("Error dispatching websocket message to service", exception);
		exceptionRecorder.tryRecordException(exception, getClass().toString());
	}

}
