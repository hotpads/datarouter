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

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.td;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.joblet.JobletPageFactory;
import io.datarouter.joblet.config.DatarouterJobletPaths;
import io.datarouter.joblet.dto.RunningJoblet;
import io.datarouter.joblet.execute.JobletProcessors;
import io.datarouter.joblet.nav.JobletLocalLinkBuilder;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.specialized.DivTag;

public class RunningJobletsHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(RunningJobletsHandler.class);

	private static final String TITLE = "Local Running Joblets";
	public static final String P_threadId = "threadId";

	@Inject
	private DatarouterJobletPaths paths;
	@Inject
	private JobletProcessors jobletProcessors;
	@Inject
	private JobletPageFactory pageFactory;
	@Inject
	private JobletLocalLinkBuilder localLinkBuilder;

	@Handler
	private Mav running(){
		List<RunningJoblet> runningJoblets = jobletProcessors.getRunningJoblets().stream()
				.sorted(Comparator.comparing(RunningJoblet::getStartedAt).reversed())
				.collect(Collectors.toList());
		return pageFactory.startBuilder(request)
				.withTitle(TITLE)
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(makeContent(runningJoblets))
				.buildMav();
	}

	@Handler
	private Mav kill(@Param(P_threadId) long threadId){
		String killedThreadProcessor = jobletProcessors.killThread(threadId)
				.orElse("thread not found");
		logger.warn("killing joblet with threadId={} -> {}", threadId, killedThreadProcessor);
		return new InContextRedirectMav(request, paths.datarouter.joblets.running.toSlashedString()
				+ "?message=" + killedThreadProcessor);
	}

	private DivTag makeContent(List<RunningJoblet> rows){
		var title = h4(TITLE)
				.withClass("mt-2");
		var table = new J2HtmlTable<RunningJoblet>()
				.withClasses("sortable table table-sm table-striped border")
				.withColumn("type", RunningJoblet::getName)
				.withColumn("id", RunningJoblet::getId)
				.withColumn("running time", RunningJoblet::getRunningTimeString)
				.withColumn("queue", RunningJoblet::getQueueId)
				.withColumn("joblet data", RunningJoblet::getJobletData)
				.withHtmlColumn("kill", row -> {
					String href = localLinkBuilder.kill(request.getContextPath(), row.getId());
					return td(a("kill").withHref(href));
				})
				.build(rows);
		return div(title, table)
				.withClass("container-fluid");
	}

}
