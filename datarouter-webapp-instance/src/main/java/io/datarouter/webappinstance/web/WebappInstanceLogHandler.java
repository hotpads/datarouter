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
package io.datarouter.webappinstance.web;

import java.util.Collections;
import java.util.Comparator;

import javax.inject.Inject;

import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.webappinstance.WebappInstanceLogService;
import io.datarouter.webappinstance.WebappInstanceLogService.WebappInstanceLogDto;
import io.datarouter.webappinstance.storage.webappinstancelog.DatarouterWebappInstanceLogDao;
import io.datarouter.webappinstance.storage.webappinstancelog.WebappInstanceLogKey;

public class WebappInstanceLogHandler extends BaseHandler{

	@Inject
	private DatarouterWebappInstanceLogDao dao;
	@Inject
	private WebappInstanceLogService webappInstanceLogService;

	@Handler(defaultHandler = true)
	public Mav webappInstanceLog(String webappName, String serverName){
		return dao.scanWithPrefix(new WebappInstanceLogKey(webappName, serverName, null, null))
				.sort(Comparator.comparing(log -> log.getKey().getStartup(), Collections.reverseOrder()))
				.map(log -> new WebappInstanceLogDto(
						log.getKey().getStartup(),
						log.getRefreshedLast(),
						log.getKey().getBuild(),
						log.getBuildId(),
						log.getCommitId(),
						log.getJavaVersion(),
						log.getServletContainerVersion(),
						log.getServerPrivateIp()))
				.listTo(logs -> webappInstanceLogService.buildMav(request, webappName, serverName, logs));
	}

}
