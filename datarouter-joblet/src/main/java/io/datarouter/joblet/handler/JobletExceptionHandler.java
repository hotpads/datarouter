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
import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.input;
import static j2html.TagCreator.script;
import static j2html.TagCreator.td;

import java.util.List;
import java.util.Optional;

import io.datarouter.joblet.JobletPageFactory;
import io.datarouter.joblet.enums.JobletStatus;
import io.datarouter.joblet.nav.JobletLocalLinkBuilder;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.JobletRequest;
import io.datarouter.joblet.type.JobletTypeFactory;
import io.datarouter.util.BooleanTool;
import io.datarouter.web.exception.ExceptionLinkBuilder;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.html.j2html.J2HtmlTable;
import io.datarouter.web.requirejs.DatarouterWebRequireJsV2;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

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
	private ExceptionLinkBuilder exceptionLinkBuilder;
	@Inject
	private JobletLocalLinkBuilder localLinkBuilder;

	@Handler
	private Mav exceptions(@Param(P_typeString) Optional<String> typeString){
		List<JobletRequest> jobletRequests = typeString
				.map(jobletTypeFactory::fromPersistentString)
				.map(jobletType -> jobletRequestDao.scanType(jobletType, true)
						.include(request -> JobletStatus.FAILED == request.getStatus())
						.list())
				.orElse(jobletRequestDao.getWithStatus(JobletStatus.FAILED));

		String contextPath = request.getContextPath();
		String deleteJobletsByIdsPath = localLinkBuilder.deleteFailedJobletsByIds(contextPath);
		String restartFailedJobletsByIdsPath = localLinkBuilder.restartFailedJobletsByIds(contextPath);

		String actionScript = """
				require(['jquery'], function($){
					$(document).ready(function() {
						function handleSelectedJoblets(path, action){
							var jobletDataIds = [];
							$('#table-failed-joblets input[type=checkbox]:checked').each(function() {
								jobletDataIds.push($(this).attr('data-joblet-data-id'));
							});
							if(jobletDataIds.length === 0){
								alert('Please select one of more rows to perform ' + action + ' operation.');
								return;
							}
							var prompt = confirm('Are you sure you want to ' + action + '?');
							if (prompt) {
								var redirectUrl = new URL(path, window.location.origin);
								var queryParams = new URLSearchParams();
								queryParams.append('placeholder_jobletDataIds', JSON.stringify(jobletDataIds));
								redirectUrl.search = queryParams;
								window.location.href = redirectUrl;
							}
						}
						
						$('#deleteButton').click(function() {
							handleSelectedJoblets('placeholder_delete_joblets_handler_path', 'delete');
						});
						
						$('#restartButton').click(function() {
							handleSelectedJoblets('placeholder_restart_joblets_handler_path', 'restart');
						});
					});
				});
				""".replace("placeholder_jobletDataIds", JobletUpdateHandler.PARAM_jobletDataIds)
				.replace("placeholder_delete_joblets_handler_path", deleteJobletsByIdsPath)
				.replace("placeholder_restart_joblets_handler_path", restartFailedJobletsByIdsPath);

		return pageFactory.startBuilder(request)
				.withTitle(TITLE)
				.withRequires(DatarouterWebRequireJsV2.SORTTABLE)
				.withContent(makeContent(jobletRequests))
				.withScript(script(actionScript))
				.buildMav();
	}

	private DivTag makeContent(List<JobletRequest> rows){
		var title = h4(TITLE)
				.withClass("mt-2");

		var buttonsDiv = div(
				button("Delete").withId("deleteButton").withClass("mx-2"),
				button("Restart").withId("restartButton").withClass("mx-2")
		).withClass("my-3 text-right");

		var table = new J2HtmlTable<JobletRequest>()
				.withId("table-failed-joblets")
				.withClasses("sortable table table-sm table-striped border")
				.withHtmlColumn("Exception ID", row -> {
					String id = row.getExceptionRecordId();
					return exceptionLinkBuilder.exception(id)
							.map(href -> td(a(id).withHref(href)))
							.orElse(td(id));
				})
				.withColumn("Type", row -> row.getKey().getType())
				.withColumn("Execution order", row -> row.getKey().getExecutionOrder(), Number::toString)
				.withColumn("Batch sequence", row -> row.getKey().getBatchSequence(), Number::toString)
				.withColumn("Data ID", JobletRequest::getJobletDataId, Number::toString)
				.withColumn("Reserved by", JobletRequest::getReservedBy)
				.withColumn("Created ago", JobletRequest::getCreatedAgo)
				.withColumn("Restartable", JobletRequest::getRestartable, BooleanTool::toString)
				.withColumn("Num items", JobletRequest::getNumItems, Number::toString)
				.withColumn("Queue ID", JobletRequest::getQueueId)
				.withHtmlColumn("Select", row -> td(input()
						.withType("checkbox")
						.attr("data-joblet-data-id", row.getJobletDataId())
				))
				.build(rows);

		return div(title, buttonsDiv, table)
				.withClass("container-fluid");
	}

}
