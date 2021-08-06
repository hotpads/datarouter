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
package io.datarouter.webappinstance.service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.util.cached.Cached;
import io.datarouter.web.app.WebappName;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstanceKey;

@Singleton
public class CachedWebappInstancesOfThisServerType extends Cached<List<WebappInstance>>{

	private static final Duration HEARTBEAT_WITHIN = Duration.ofMinutes(3);

	private final DatarouterProperties datarouterProperties;
	private final WebappName webappName;
	private final DatarouterWebappInstanceDao webappInstanceDao;

	@Inject
	public CachedWebappInstancesOfThisServerType(
			DatarouterProperties datarouterProperties,
			WebappName webappName,
			DatarouterWebappInstanceDao webappInstanceDao){
		super(20, TimeUnit.SECONDS);
		this.datarouterProperties = datarouterProperties;
		this.webappName = webappName;
		this.webappInstanceDao = webappInstanceDao;
	}

	public List<String> getSortedServerNamesForThisWebApp(){
		return getSortedServerNamesForWebAppName(webappName.getName());
	}

	public List<String> getSortedServerNamesForWebAppName(String webappName){
		return Scanner.of(get())
				.map(WebappInstance::getKey)
				.include(key -> key.getWebappName().equals(webappName))
				.map(WebappInstanceKey::getServerName)
				.sort()
				.list();
	}

	@Override
	protected List<WebappInstance> reload(){
		return webappInstanceDao.getWebappInstancesOfServerType(
				datarouterProperties.getServerType(),
				HEARTBEAT_WITHIN);
	}

}
