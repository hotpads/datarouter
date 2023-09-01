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

import java.util.Collection;

import io.datarouter.joblet.JobletPageFactory;
import io.datarouter.joblet.dto.JobletSummary;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.storage.jobletrequest.JobletRequestKey;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletTypeFactory;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class JobletQueuesHandler extends BaseHandler{

	private static final String TITLE = "Joblet Queues";
	public static final String P_jobletType = "jobletType";
	public static final String P_executionOrder = "executionOrder";

	@Inject
	private JobletTypeFactory jobletTypeFactory;
	@Inject
	private DatarouterJobletRequestDao jobletRequestDao;
	@Inject
	private JobletPageFactory pageFactory;

	@Handler
	private Mav queues(
			@Param(P_jobletType) String jobletType,
			@Param(P_executionOrder) Integer executionOrder){
		JobletType<?> type = jobletTypeFactory.fromPersistentString(jobletType);
		JobletRequestKey prefix = new JobletRequestKey(type.getPersistentString(), executionOrder, null, null);
		Scanner<JobletRequest> requests = jobletRequestDao.scanWithPrefix(prefix);
		Collection<JobletSummary> summaries = JobletSummary.summarizeByQueueStatus(requests).values();
		return pageFactory.startBuilder(request)
				.withTitle(TITLE)
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(makeContent(type, executionOrder, summaries))
				.buildMav();
	}

	private DivTag makeContent(JobletType<?> type, Integer executionOrder, Collection<JobletSummary> rows){
		var title = h4(TITLE)
				.withClass("mt-2");
		var subtitle = p(String.format("type: %s, executionOrder: %s", type.getPersistentString(), executionOrder));
		var table = new J2HtmlTable<JobletSummary>()
				.withClasses("sortable table table-sm table-striped border")
				.withColumn("queueId", JobletSummary::getQueueId)
				.withColumn("status", row -> row.getStatus().persistentString)
				.withColumn("numFailures", JobletSummary::getNumFailures, NumberFormatter::addCommas)
				.withColumn("numJoblets", JobletSummary::getNumType, NumberFormatter::addCommas)
				.withColumn("firstReserved", JobletSummary::getFirstReservedAgo)
				.withColumn("firstCreated", JobletSummary::getFirstCreatedAgo)
				.withColumn("sumItems", JobletSummary::getSumItems, NumberFormatter::addCommas)
				.withColumn("avgItems", JobletSummary::getAvgItems, avg -> NumberFormatter.format(avg, 1))
				.build(rows);
		return div(title, subtitle, table)
				.withClass("container-fluid");
	}

}
