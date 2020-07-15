/**
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
package io.datarouter.webappinstance;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.instrumentation.webappinstance.WebappInstancePublisher;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.web.listener.DatarouterAppListener;
import io.datarouter.webappinstance.config.DatarouterWebappInstanceSettingRoot;
import io.datarouter.webappinstance.service.WebappInstanceService;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstanceKey;

@Singleton
public class WebappInstanceAppListener implements DatarouterAppListener{
	private static final Logger logger = LoggerFactory.getLogger(WebappInstanceAppListener.class);

	@Inject
	private DatarouterWebappInstanceDao dao;
	@Inject
	private WebappInstanceService service;
	@Inject
	private WebappInstancePublisher publisher;
	@Inject
	private DatarouterWebappInstanceSettingRoot settings;
	@Inject
	private ServerTypeDetector serverTypeDetector;

	@Override
	public void onStartUp(){
		try{
			service.updateWebappInstanceTable();
		}catch(Exception e){
			// on start up exceptions might be thrown
			logger.info("on start up {}", e.getMessage());
		}
	}

	@Override
	public void onShutDown(){
		WebappInstanceKey key = service.buildCurrentWebappInstanceKey();
		dao.delete(key);
		if(!settings.webappInstancePublisher.get()){
			logger.warn("WebappInstancePublisher is disabled on client");
			return;
		}
		try{
			logger.info("external webapp deregistration start");
			PublishingResponseDto response = publisher.delete(key.getWebappName(), key.getServerName());
			if(!response.success){
				logger.warn("error on webapp deregistration. message={}", response.message);
			}else{
				logger.warn("external webapp deregistration complete");
			}
		}catch(Exception e){
			// on dev environments exceptions might be thrown
			if(serverTypeDetector.mightBeDevelopment()){
				logger.info("", e);
			}else{
				logger.warn("", e);
			}
		}
	}

}
