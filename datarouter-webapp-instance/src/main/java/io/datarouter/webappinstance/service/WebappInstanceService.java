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
package io.datarouter.webappinstance.service;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.instrumentation.webappinstance.WebappInstanceDto;
import io.datarouter.instrumentation.webappinstance.WebappInstancePublisher;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.util.SystemTool;
import io.datarouter.web.app.WebappName;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.monitoring.BuildProperties;
import io.datarouter.web.monitoring.GitProperties;
import io.datarouter.web.port.CompoundPortIdentifier;
import io.datarouter.webappinstance.config.DatarouterWebappInstanceSettingRoot;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstanceKey;
import io.datarouter.webappinstance.storage.webappinstancelog.DatarouterWebappInstanceLogDao;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLog;

@Singleton
public class WebappInstanceService{

	private final DatarouterWebappInstanceDao webappInstanceDao;
	private final DatarouterWebappInstanceLogDao webappInstanceLogDao;
	private final WebappName webappName;
	private final GitProperties gitProperties;
	private final BuildProperties buildProperties;
	private final DatarouterProperties datarouterProperties;
	private final ServletContextSupplier servletContext;
	private final DatarouterWebappInstanceSettingRoot settings;
	private final WebappInstancePublisher webappInstancePublisher;
	private final CompoundPortIdentifier portIdentifier;

	@Deprecated
	private final Date startTime;
	private final Instant startup;

	@Inject
	public WebappInstanceService(
			DatarouterWebappInstanceDao webappInstanceDao,
			DatarouterWebappInstanceLogDao webappInstanceLogDao,
			WebappName webappName,
			GitProperties gitProperties,
			BuildProperties buildProperties,
			DatarouterProperties datarouterProperties,
			ServletContextSupplier servletContext,
			WebappInstancePublisher webappInstancePublisher,
			DatarouterWebappInstanceSettingRoot settings,
			CompoundPortIdentifier portIdentifier){
		this.webappInstanceDao = webappInstanceDao;
		this.webappInstanceLogDao = webappInstanceLogDao;
		this.webappName = webappName;
		this.gitProperties = gitProperties;
		this.buildProperties = buildProperties;
		this.datarouterProperties = datarouterProperties;
		this.servletContext = servletContext;
		this.webappInstancePublisher = webappInstancePublisher;
		this.settings = settings;
		this.portIdentifier = portIdentifier;

		this.startTime = new Date(ManagementFactory.getRuntimeMXBean().getStartTime());
		this.startup = Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime());
	}

	public List<WebappInstance> getAll(){
		return webappInstanceDao.scan().list();
	}

	public WebappInstance updateWebappInstanceTable(){
		String buildId = buildProperties.getBuildId();
		String commitId = gitProperties.getIdAbbrev().orElse(GitProperties.UNKNOWN_STRING);
		Counters.inc("App heartbeat " + datarouterProperties.getServerTypeString());
		Counters.inc("App heartbeat type-build " + datarouterProperties.getServerTypeString() + " " + buildId);
		Counters.inc("App heartbeat type-commit " + datarouterProperties.getServerTypeString() + " " + commitId);
		Counters.inc("App heartbeat build " + buildId);
		Counters.inc("App heartbeat commit " + commitId);
		WebappInstance webappInstance = buildCurrentWebappInstance();
		webappInstanceDao.put(webappInstance);
		webappInstanceLogDao.put(new WebappInstanceLog(webappInstance));
		if(settings.webappInstancePublisher.get()){
			WebappInstanceDto dto = webappInstance.toDto();
			webappInstancePublisher.add(dto);
		}
		return webappInstance;
	}

	public WebappInstance buildCurrentWebappInstance(){
		return new WebappInstance(
				webappName.getName(),
				datarouterProperties.getServerName(),
				datarouterProperties.getServerTypeString(),
				servletContext.get().getContextPath(),
				datarouterProperties.getServerPublicIp(),
				datarouterProperties.getServerPrivateIp(),
				startTime,
				Date.from(gitProperties.getBuildTime().orElse(GitProperties.UNKNOWN_DATE)),
				startup,
				gitProperties.getBuildTime().orElse(GitProperties.UNKNOWN_DATE),
				buildProperties.getBuildId(),
				gitProperties.getIdAbbrev().orElse(GitProperties.UNKNOWN_STRING),
				SystemTool.getJavaVersion(),
				servletContext.get().getServerInfo(),
				gitProperties.getBranch().orElse(GitProperties.UNKNOWN_STRING),
				portIdentifier.getHttpsPort());
	}

	public WebappInstanceKey buildCurrentWebappInstanceKey(){
		return new WebappInstanceKey(webappName.getName(), datarouterProperties.getServerName());
	}

}
