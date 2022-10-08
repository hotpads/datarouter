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
package io.datarouter.auth.service;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.config.DatarouterAuthSettingRoot;
import io.datarouter.httpclient.endpoint.caller.CallerType;
import io.datarouter.httpclient.endpoint.java.BaseEndpoint;
import io.datarouter.util.lang.ReflectionTool;
import io.datarouter.web.handler.BaseHandler.Handler;
import io.datarouter.web.handler.validator.HandlerAccountCallerValidator;

@Singleton
public class DefaultHandlerAccountCallerValidator implements HandlerAccountCallerValidator{
	private static final Logger logger = LoggerFactory.getLogger(DefaultHandlerAccountCallerValidator.class);

	private static final Set<String> logs = ConcurrentHashMap.newKeySet();

	@Inject
	private CallerTypeByAccountNameCache callerTypeByAccountNameCache;
	@Inject
	private DatarouterAuthSettingRoot settings;

	@Override
	public void validate(String accountName, BaseEndpoint<?,?> endpoint){
		if(!settings.enableHandlerAccountCallerValidator.get()){
			return;
		}
		try{
			// could be null if not set through the UI
			String accountCallerType = callerTypeByAccountNameCache.get(accountName);
			Class<? extends CallerType> endpointCallerTypeClass = endpoint.callerType;
			String endpointCallerType = ReflectionTool.create(endpointCallerTypeClass).getName();
			if(accountCallerType == null || !accountCallerType.equals(endpointCallerType)){
				String message = String.format("EndpointName=%s accountName=%s accountCallerType=%s "
						+ "endpointCallerType=%s",
						endpoint.getClass().getSimpleName(),
						accountName,
						accountCallerType,
						endpointCallerType);
				if(logs.add(message)){
					logger.info(message);
				}
			}
		}catch(Exception ex){
			logger.error("", ex);
		}
	}

	@Override
	public void validate(String accountName, Method method){
		if(!settings.enableHandlerAccountCallerValidator.get()){
			return;
		}
		try{
			// could be null if not set through the UI
			String accountCallerType = callerTypeByAccountNameCache.get(accountName);
			Class<? extends CallerType> handlerCallerTypeClass = method.getAnnotation(Handler.class).callerType();
			String handlerCallerType = ReflectionTool.create(handlerCallerTypeClass).getName();
			if(accountCallerType == null || !accountCallerType.equals(handlerCallerType)){
				String message = String.format("HandlerClass=%s accountName=%s accountCallerType=%s "
						+ "endpointCallerType=%s",
						method.getDeclaringClass().getName() + "." + method.getName(),
						accountName,
						accountCallerType,
						handlerCallerType);
				if(logs.add(message)){
					logger.info(message);
				}
			}
		}catch(Exception ex){
			logger.error("", ex);
		}
	}

}
