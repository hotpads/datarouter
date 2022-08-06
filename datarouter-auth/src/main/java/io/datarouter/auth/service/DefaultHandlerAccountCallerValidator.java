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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.auth.config.DatarouterAuthSettingRoot;
import io.datarouter.httpclient.endpoint.BaseEndpoint;
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
			String endpointCallerType = endpoint.getCallerType().name;
			if(accountCallerType == null || !accountCallerType.equals(endpointCallerType)){
				String message = String.format("EndpointName=%s accountName=%s accountCallerType=%s "
						+ "endpointCallerType=%s",
						endpoint.getClass().getSimpleName(),
						accountName,
						accountCallerType,
						endpoint.getCallerType().name);
				if(logs.add(message)){
					logger.info(message);
				}
			}
		}catch(Exception ex){
			logger.error("", ex);
		}
	}

}
