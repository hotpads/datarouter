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
package io.datarouter.joblet.handler;

import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.p;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.joblet.JobletPageFactory;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.joblet.service.JobletService.JobletServiceThreadCountResponse;
import io.datarouter.joblet.setting.DatarouterJobletSettingRoot;
import io.datarouter.joblet.type.JobletTypeFactory;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import io.datarouter.webappinstance.service.CachedWebappInstancesOfThisServerType;
import j2html.tags.ContainerTag;

public class JobletThreadCountHandler extends BaseHandler{

	private static final String TITLE = "Joblet Thread Counts";

	@Inject
	private CachedWebappInstancesOfThisServerType cachedWebAppInstancesOfThisServerType;
	@Inject
	private JobletService jobletService;
	@Inject
	private JobletTypeFactory jobletTypeFactory;
	@Inject
	private DatarouterJobletSettingRoot jobletSettings;
	@Inject
	private JobletPageFactory pageFactory;

	@Handler
	private Mav threadCounts(){
		List<String> serverNames = cachedWebAppInstancesOfThisServerType.getSortedServerNamesForThisWebApp();
		List<JobletServiceThreadCountResponse> threadCountResponses = jobletTypeFactory.getAllTypes().stream()
				.map(jobletService::getThreadCountInfoForThisInstance)
				.collect(Collectors.toList());
		var content = makeContent(
				serverNames,
				jobletSettings.maxJobletServers.get(),
				jobletSettings.maxJobletServers.get(),
				threadCountResponses);
		return pageFactory.startBuilder(request)
				.withTitle(TITLE)
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	private ContainerTag makeContent(
			List<String> serverNames,
			int minServers,
			int maxServers,
			List<JobletServiceThreadCountResponse> rows){
		var title = h4(TITLE)
				.withClass("mt-2");
		var subtitle = p(String.format("numServers: current: %s, min: %s, max: %s",
				serverNames.size(),
				minServers,
				maxServers));
		var table = new J2HtmlTable<JobletServiceThreadCountResponse>()
				.withClasses("sortable table table-sm table-striped border")
				.withColumn("jobletType", row -> row.jobletType.getPersistentString())
				.withColumn("clusterLimit", row -> row.clusterLimit)
				.withColumn("instanceAvg", row -> row.clusterLimit / (double)serverNames.size())
				.withColumn("instanceLimit", row -> row.instanceLimit)
				.withColumn("numExtraThreads", row -> row.numExtraThreads)
				.withColumn("firstExtraInstanceIndex", row -> row.firstExtraInstanceIdxInclusive)
				.withColumn("firstExtraInstanceServerName", row -> serverNames.get(row.firstExtraInstanceIdxInclusive))
				.withColumn("thisInstanceRunsExtraThread", row -> row.runExtraThread)
				.build(rows);
		return div(title, subtitle, table)
				.withClass("container-fluid");
	}

}
