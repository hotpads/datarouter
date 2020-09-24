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
package io.datarouter.job.monitoring;

import static j2html.TagCreator.a;
import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.span;

import java.time.Duration;
import java.util.List;

import javax.inject.Inject;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.job.config.DatarouterJobPaths;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.tasktracker.storage.DatarouterLongRunningTaskDao;
import io.datarouter.tasktracker.storage.LongRunningTask;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.html.email.J2HtmlDatarouterEmailBuilder;
import io.datarouter.web.html.email.J2HtmlEmailTable;
import io.datarouter.web.html.email.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import j2html.tags.ContainerTag;

public class LongRunningTaskFailureAlertJob extends BaseJob{

	@Inject
	private DatarouterAdministratorEmailService adminEmailService;
	@Inject
	private DatarouterHtmlEmailService emailService;
	@Inject
	private DatarouterJobPaths paths;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterLongRunningTaskDao longRunningTaskDao;

	@Override
	public void run(TaskTracker tracker){
		longRunningTaskDao.scan()
				.include(task -> task.getStartTime() == null
				|| task.getStartTime().getTime() > System.currentTimeMillis() - Duration.ofDays(1).toMillis())
				.include(task -> task.isBadState())
				.flush(this::sendEmail);
	}

	private void sendEmail(List<LongRunningTask> longRunningTaskList){
		if(longRunningTaskList.size() > 0){
			String fromEmail = datarouterProperties.getAdministratorEmail();
			String toEmail = adminEmailService.getAdministratorEmailAddressesCsv();
			String primaryHref = emailService.startLinkBuilder()
					.withLocalPath(paths.datarouter.triggers)
					.build();
			ContainerTag content = buildEmail(datarouterProperties.getServerName(),
					longRunningTaskList);
			J2HtmlDatarouterEmailBuilder emailBuilder = emailService.startEmailBuilder()
					.withTitle("LongRunningTaskFailure")
					.withTitleHref(primaryHref)
					.withContent(content);
			emailService.trySendJ2Html(fromEmail, toEmail, emailBuilder);
		}
	}

	private ContainerTag buildEmail(String serverNameString,
			List<LongRunningTask> longRunningTaskList){
		var body = body();
		String headerVerb = longRunningTaskList.size() == 1 ? " is " : " are ";
		var header = h4("There" + headerVerb + longRunningTaskList.size()
				+ " non-successful long running tasks in the last 24 hours.");
		ContainerTag taskTable = new J2HtmlEmailTable<LongRunningTask>()
				.withColumn("NAME", row -> row.getKey().getName())
				.withColumn("START TIME", row -> row.getStartTime())
				.withColumn("DURATION", row -> row.getDurationString())
				.withColumn("TRIGGERED BY", row -> row.getTriggeredBy())
				.withColumn("STATUS", row -> row.getJobExecutionStatus().getPersistentString())
				.withColumn(new J2HtmlEmailTableColumn<>("EXCEPTION RECORD ID",
						row -> makeExceptionLink(row.getExceptionRecordId())))
				.build(longRunningTaskList);
		body.with(div(header, taskTable));
		return body.with(
				br(),
				br(),
				span("Sent from: " + serverNameString));
	}

	private ContainerTag makeExceptionLink(String exceptionRecordId){
		String href = emailService.startLinkBuilder()
				.withLocalPath("/datarouter/exception/details?")
				.withParam("exceptionRecord", exceptionRecordId)
				.build();
		return a(exceptionRecordId)
				.withHref(href);
	}

}
