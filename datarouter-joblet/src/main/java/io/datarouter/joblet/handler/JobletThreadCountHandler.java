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

import io.datarouter.joblet.JobletPageFactory;
import io.datarouter.joblet.service.JobletService;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletTypeFactory;
import io.datarouter.util.BooleanTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import io.datarouter.webappinstance.service.CachedWebappInstancesOfThisServerType;
import io.datarouter.webappinstance.service.ClusterThreadCountService.InstanceThreadCounts;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class JobletThreadCountHandler extends BaseHandler{

	private static final String TITLE = "Joblet Thread Counts";

	@Inject
	private CachedWebappInstancesOfThisServerType cachedWebAppInstancesOfThisServerType;
	@Inject
	private JobletService jobletService;
	@Inject
	private JobletTypeFactory jobletTypeFactory;
	@Inject
	private JobletPageFactory pageFactory;

	@Handler
	private Mav threadCounts(){
		List<String> serverNames = cachedWebAppInstancesOfThisServerType.getSortedServerNamesForThisWebApp();
		List<JobletTypeThreadCounts> threadCountResponses = jobletTypeFactory.getAllTypes().stream()
				.map(jobletType -> new JobletTypeThreadCounts(
						jobletType,
						jobletService.getThreadCountInfoForThisInstance(jobletType)))
				.toList();
		var content = makeContent(
				serverNames,
				threadCountResponses);
		return pageFactory.startBuilder(request)
				.withTitle(TITLE)
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(content)
				.buildMav();
	}

	private DivTag makeContent(
			List<String> serverNames,
			List<JobletTypeThreadCounts> rows){
		var title = h4(TITLE)
				.withClass("mt-2");
		var subtitle = p(String.format("numServers: %s", serverNames.size()));
		var table = new J2HtmlTable<JobletTypeThreadCounts>()
				.withClasses("sortable table table-sm table-striped border")
				.withColumn("jobletType", row -> row.jobletType().getPersistentString())
				.withColumn("clusterLimit", row -> row.threadCounts().clusterLimit(), Number::toString)
				.withColumn("instanceAvg", row -> row.threadCounts().clusterLimit() / (double)serverNames.size(),
						Number::toString)
				.withColumn("instanceLimit", row -> row.threadCounts().instanceLimit(), Number::toString)
				.withColumn("numExtraThreads", row -> row.threadCounts().numExtraThreads(), Number::toString)
				.withColumn("firstExtraInstanceIndex", row -> row.threadCounts().firstExtraInstanceIdxInclusive(),
						Number::toString)
				.withColumn("firstExtraInstanceServerName", row -> serverNames.get(row.threadCounts()
						.firstExtraInstanceIdxInclusive()))
				.withColumn("thisInstanceRunsExtraThread", row -> row.threadCounts().runExtraThread(),
						BooleanTool::toString)
				.build(rows);
		return div(title, subtitle, table)
				.withClass("container-fluid");
	}

	public record JobletTypeThreadCounts(
			JobletType<?> jobletType,
			InstanceThreadCounts threadCounts){
	}

}
