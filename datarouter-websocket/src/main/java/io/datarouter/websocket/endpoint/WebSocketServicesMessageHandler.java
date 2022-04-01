/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.websocket.endpoint;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.MessageHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.string.StringTool;
import io.datarouter.web.exception.ExceptionRecorder;
import io.datarouter.websocket.storage.session.WebSocketSession;

public class WebSocketServicesMessageHandler implements ClosableMessageHandler, MessageHandler.Whole<String>{
	private static final Logger logger = LoggerFactory.getLogger(WebSocketServicesMessageHandler.class);

	private final WebSocketServices services;
	private final ExceptionRecorder exceptionRecorder;

	private final WebSocketSession webSocketSession;
	private final Map<String,WebSocketService> openedServices;

	@Singleton
	public static class WebSocketServicesMessageHandlerFactory{

		@Inject
		private WebSocketServices services;
		@Inject
		private ExceptionRecorder exceptionRecorder;

		public WebSocketServicesMessageHandler create(WebSocketSession webSocketSession){
			return new WebSocketServicesMessageHandler(services, exceptionRecorder, webSocketSession);
		}

	}

	private WebSocketServicesMessageHandler(WebSocketServices services, ExceptionRecorder exceptionRecorder,
			WebSocketSession webSocketSession){
		this.services = services;
		this.exceptionRecorder = exceptionRecorder;
		this.webSocketSession = webSocketSession;
		this.openedServices = new HashMap<>();
	}

	@Override
	public void onMessage(String message){
		int prefixEndIndex = message.indexOf("|");
		if(prefixEndIndex == -1){
			handleError("service not specified message=" + message);
			return;
		}
		String serviceString = message.substring(0, prefixEndIndex);
		if(StringTool.isEmpty(serviceString)){
			handleError("service not specified message=" + message);
			return;
		}
		WebSocketService service = openedServices.get(serviceString);
		if(service == null){
			service = services.getNewInstance(serviceString);
		}
		if(service == null){
			handleError("service not found serviceName=" + serviceString);
			return;
		}
		openedServices.put(serviceString, service);
		String payload = message.substring(prefixEndIndex + 1);
		try{
			service.onMessage(webSocketSession, payload);
		}catch(Exception exception){
			logger.warn("Exception in websocket service handling message", exception);
			exceptionRecorder.tryRecordException(exception, service.getClass().getName());
		}
	}

	private void handleError(String errorMessage){
		Exception exception = new Exception(errorMessage);
		logger.warn("Error dispatching websocket message to service", exception);
		exceptionRecorder.tryRecordException(exception, getClass().getName());
	}

	@Override
	public void onClose(){
		openedServices.values().forEach(service -> service.onClose(webSocketSession));
	}

}
