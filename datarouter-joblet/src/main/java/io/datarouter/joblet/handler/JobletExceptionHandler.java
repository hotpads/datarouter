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

import java.util.List;

import javax.inject.Inject;

import io.datarouter.joblet.JobletPageFactory;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.nav.JobletExternalLinkBuilder;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.type.JobletTypeFactory;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.ContainerTag;

public class JobletExceptionHandler extends BaseHandler{

	private static final String TITLE = "Joblet Exceptions";
	public static final String P_typeString = "typeString";

	@Inject
	private JobletTypeFactory jobletTypeFactory;
	@Inject
	private DatarouterJobletRequestDao jobletRequestDao;
	@Inject
	private JobletPageFactory pageFactory;
	@Inject
	private JobletExternalLinkBuilder externalLinkBuilder;

	@Handler
	private Mav exceptions(@Param(P_typeString) OptionalString typeString){
		List<JobletRequest> jobletRequests = typeString
				.map(jobletTypeFactory::fromPersistentString)
				.map(jobletType -> jobletRequestDao.scanType(jobletType, true)
						.include(request -> JobletStatus.FAILED == request.getStatus())
						.list())
				.orElse(jobletRequestDao.getWithStatus(JobletStatus.FAILED));
		return pageFactory.startBuilder(request)
				.withTitle(TITLE)
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(makeContent(jobletRequests))
				.buildMav();
	}

	private ContainerTag makeContent(List<JobletRequest> rows){
		var title = h4(TITLE)
				.withClass("mt-2");
		var table = new J2HtmlTable<JobletRequest>()
				.withClasses("sortable table table-sm table-striped border")
				.withHtmlColumn("Exception ID", row -> {
					String id = row.getExceptionRecordId();
					return externalLinkBuilder.exception(request.getContextPath(), id)
							.map(href -> td(a(id).withHref(href)))
							.orElse(td(id));
				})
				.withColumn("Type", row -> row.getKey().getType())
				.withColumn("Execution order", row -> row.getKey().getExecutionOrder())
				.withColumn("Batch sequence", row -> row.getKey().getBatchSequence())
				.withColumn("Data ID", row -> row.getJobletDataId())
				.withColumn("Reserved by", row -> row.getReservedBy())
				.withColumn("Created ago", row -> row.getCreatedAgo())
				.withColumn("Restartable", row -> row.getRestartable())
				.withColumn("Num items", row -> row.getNumItems())
				.withColumn("Queue ID", row -> row.getQueueId())
				.build(rows);
		return div(title, table)
				.withClass("container-fluid");
	}

}
