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
package io.datarouter.tasktracker.service;

import static j2html.TagCreator.a;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.i;
import static j2html.TagCreator.small;
import static j2html.TagCreator.td;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.tasktracker.config.DatarouterTaskTrackerPaths;
import io.datarouter.tasktracker.storage.DatarouterLongRunningTaskDao;
import io.datarouter.tasktracker.storage.LongRunningTask;
import io.datarouter.tasktracker.web.TaskTrackerExceptionLink;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.html.j2html.J2HtmlTable;
import j2html.TagCreator;
import j2html.tags.ContainerTag;

@Singleton
public class LongRunningTaskDailyDigest implements DailyDigest{

	@Inject
	private DatarouterLongRunningTaskDao longRunningTaskDao;
	@Inject
	private TaskTrackerExceptionLink exceptionLink;
	@Inject
	private DatarouterHtmlEmailService emailService;
	@Inject
	private DatarouterTaskTrackerPaths taskPaths;
	@Inject
	private ServletContextSupplier contextSupplier;

	@Override
	public Optional<ContainerTag> getContent(){
		List<LongRunningTask> failedTasks = longRunningTaskDao.scan()
				.include(task -> task.getKey().getTriggerTime().getTime()
						> System.currentTimeMillis() - Duration.ofDays(1).toMillis())
				.include(LongRunningTask::isBadState)
				.list();
		if(failedTasks.isEmpty()){
			return Optional.empty();
		}
		var header = h3("Failed Long Running Tasks");
		var description = small("From the last 24 hours");
		var table = buildTable(failedTasks);
		return Optional.of(TagCreator.div(header, description, table));
	}

	private ContainerTag buildTable(List<LongRunningTask> rows){
		return new J2HtmlTable<LongRunningTask>()
				.withClasses("sortable table table-sm table-striped my-4 border")
				.withHtmlColumn("Name", row -> makeTaskLink(row.getKey().getName()))
				.withColumn("Trigger Time", row -> row.getKey().getTriggerTime())
				.withColumn("Duration", row -> row.getDurationString())
				.withColumn("Triggered By", row -> row.getTriggeredBy())
				.withColumn("Status", row -> row.getJobExecutionStatus().getPersistentString())
				.withHtmlColumn("Exception", row -> makeExceptionLink(row.getExceptionRecordId()))
				.build(rows);
	}

	private ContainerTag makeTaskLink(String longRunningTaskName){
		String href = emailService.startLinkBuilder()
				.withLocalPath(taskPaths.datarouter.longRunningTasks)
				.withParam("name", longRunningTaskName)
				.withParam("status", "all")
				.build();
		return td(a(longRunningTaskName)
				.withHref(href));
	}

	private ContainerTag makeExceptionLink(String exceptionRecordId){
		String href = contextSupplier.get().getContextPath() + exceptionLink.buildExceptionDetailLink(exceptionRecordId);
		return td(a(i().withClass("far fa-file-alt"))
				.withClass("btn btn-link w-100 py-0")
				.withTarget("_blank")
				.withHref(href));
	}

}
