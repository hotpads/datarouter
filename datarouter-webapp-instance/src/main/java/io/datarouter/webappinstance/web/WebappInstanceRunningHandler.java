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

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.user.session.CurrentUserSessionInfoService;
import io.datarouter.webappinstance.WebappInstanceTableService;
import io.datarouter.webappinstance.WebappInstanceTableService.WebappInstanceColumn;
import io.datarouter.webappinstance.WebappInstanceTableService.WebappInstanceTableOptions;
import io.datarouter.webappinstance.WebappInstanceTableTool;
import io.datarouter.webappinstance.service.WebappInstanceBuildIdLink;
import io.datarouter.webappinstance.service.WebappInstanceCommitIdLink;
import io.datarouter.webappinstance.storage.webappinstance.DatarouterWebappInstanceDao;
import io.datarouter.webappinstance.storage.webappinstance.WebappInstance;

public class WebappInstanceRunningHandler extends BaseHandler{

	public static final String P_USER_ID = "userId";
	public static final String P_TOKEN = "token";

	@Inject
	private DatarouterWebappInstanceDao dao;
	@Inject
	private WebappInstanceBuildIdLink buildIdLink;
	@Inject
	private WebappInstanceCommitIdLink commitIdLink;
	@Inject
	private CurrentUserSessionInfoService currentSessionInfoService;
	@Inject
	private WebappInstanceTableService webappInstanceTableService;

	@Handler
	public Mav running(){
		ZoneId zoneId = currentSessionInfoService.getZoneId(request);
		List<WebappInstance> webappInstances = dao.scan()
				.sort(Comparator.comparing(webappInstance -> webappInstance.getKey().getServerName()))
				.list();

		Map<String,String> mostCommonCommitByWebapp = webappInstances.stream()
				.collect(Collectors.groupingBy(instance -> instance.getKey().getWebappName(),
						Collectors.collectingAndThen(Collectors.toList(),
								instances -> WebappInstanceTableService.getMostCommonValue(instances,
										WebappInstance::getCommitId))));

		return webappInstanceTableService.buildMav(
				request,
				webappInstances,
				new WebappInstanceTableOptions(),
				List.of(
					new WebappInstanceColumn<WebappInstance>("Service", inst -> inst.getKey().getWebappName())
							.withShowUsageStats()
							.withHideUsageStatsBreakdown(),
					new WebappInstanceColumn<WebappInstance>("Server Name", inst -> inst.getKey().getServerName()),
					new WebappInstanceColumn<>("Server Type", WebappInstance::getServerType),
					new WebappInstanceColumn<>("Public IP", WebappInstance::getServerPublicIp)
							.withShowUsageStats(),
					new WebappInstanceColumn<>("Private IP", WebappInstance::getServerPrivateIp)
							.withCellLinkBuilder(false, inst -> "https://" + inst.getServerPrivateIp() + ":"
									+ inst.getHttpsPort() + inst.getServletContextPath()),
					WebappInstanceTableTool.uptime(WebappInstance::getStartupInstant, zoneId),
					WebappInstanceTableTool.lastUpdated(WebappInstance::getRefreshedLastInstant),
					WebappInstanceTableTool.buildDate(WebappInstance::getBuildInstant, zoneId),
					new WebappInstanceColumn<>("Build Id", WebappInstance::getBuildId)
							.withShowUsageStats()
							.withAsBadge((inst, stats) -> !Objects.equals(inst.getBuildId(), stats.mostCommon))
							.withStatLinkBuilder(true, buildIdLink::getLink),
					new WebappInstanceColumn<>("Commit Id", WebappInstance::getCommitId)
							.withShowUsageStats()
							.withAsBadge((inst, stats) -> !mostCommonCommitByWebapp.get(inst.getKey().getWebappName())
									.equals(inst.getCommitId()))
							.withStatLinkBuilder(true, commitIdLink.getLinkPrefix()::concat),
					new WebappInstanceColumn<>("Java Version", WebappInstance::getJavaVersion)
							.withShowUsageStats()
							.withAsBadge((inst, stats) -> !Objects.equals(inst.getJavaVersion(), stats.mostCommon)),
					new WebappInstanceColumn<>("Servlet Container", WebappInstance::getServletContainerVersion)
							.withShowUsageStats()
							.withAsBadge((inst, stats) -> !Objects.equals(inst.getServletContainerVersion(),
									stats.mostCommon))));
	}

}
