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
package io.datarouter.job.monitoring;

import static j2html.TagCreator.a;
import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.span;

import java.time.Duration;
import java.util.List;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.html.J2HtmlDatarouterEmailBuilder;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.email.type.DatarouterEmailTypes.LongRunningTaskFailureAlertEmailType;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.job.BaseJob;
import io.datarouter.job.config.DatarouterJobPaths;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.tasktracker.config.DatarouterTaskTrackerPaths;
import io.datarouter.tasktracker.storage.LongRunningTask;
import io.datarouter.tasktracker.storage.LongRunningTaskDao;
import io.datarouter.tasktracker.web.TaskTrackerExceptionLink;
import io.datarouter.web.config.properties.DefaultEmailDistributionListZoneId;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.BodyTag;
import j2html.tags.specialized.TableTag;
import jakarta.inject.Inject;

public class LongRunningTaskFailureAlertJob extends BaseJob{

	@Inject
	private DatarouterHtmlEmailService emailService;
	@Inject
	private DatarouterJobPaths paths;
	@Inject
	private ServerName serverName;
	@Inject
	private LongRunningTaskDao longRunningTaskDao;
	@Inject
	private DatarouterTaskTrackerPaths taskPaths;
	@Inject
	private TaskTrackerExceptionLink exceptionLink;
	@Inject
	private LongRunningTaskFailureAlertEmailType longRunningTaskFailureAlertEmailType;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;
	@Inject
	private DefaultEmailDistributionListZoneId defaultDistributionListZoneId;

	@Override
	public void run(TaskTracker tracker){
		longRunningTaskDao.scan()
				.include(task -> task.getKey().getTriggerTime().toEpochMilli()
						> System.currentTimeMillis() - Duration.ofDays(1).toMillis())
				.include(LongRunningTask::isBadState)
				.flush(this::sendEmail);
	}

	private void sendEmail(List<LongRunningTask> longRunningTaskList){
		if(!longRunningTaskList.isEmpty()){
			String primaryHref = emailService.startLinkBuilder()
					.withLocalPath(paths.datarouter.triggers)
					.build();
			BodyTag content = buildEmail(serverName.get(), longRunningTaskList);
			J2HtmlDatarouterEmailBuilder emailBuilder = emailService.startEmailBuilder()
					.withTitle("LongRunningTaskFailure")
					.withTitleHref(primaryHref)
					.withContent(content)
					.fromAdmin()
					.toSubscribers(serverTypeDetector.mightBeProduction())
					.to(longRunningTaskFailureAlertEmailType.tos, serverTypeDetector.mightBeProduction())
					.toAdmin(!serverTypeDetector.mightBeProduction());
			emailService.trySendJ2Html(emailBuilder);
		}
	}

	private BodyTag buildEmail(
			String serverNameString,
			List<LongRunningTask> longRunningTaskList){
		var body = body();
		String headerVerb = longRunningTaskList.size() == 1 ? " is " : " are ";
		var header = standardDatarouterEmailHeaderService.makeStandardHeader();
		var description = h4("There" + headerVerb + longRunningTaskList.size()
				+ " non-successful long running tasks in the last 24 hours.");
		TableTag taskTable = new J2HtmlEmailTable<LongRunningTask>()
				.withColumn(new J2HtmlEmailTableColumn<>("Name",
						row -> makeTaskLink(row.getKey().getName())))
				.withColumn("Trigger Time", row -> row.getKey().getTriggerTime()
						.format(defaultDistributionListZoneId.get()))
				.withColumn("Duration", LongRunningTask::getDurationString)
				.withColumn("Triggered By", LongRunningTask::getTriggeredBy)
				.withColumn("Status", row -> row.getJobExecutionStatus().persistentString)
				.withColumn(new J2HtmlEmailTableColumn<>("Exception Record Id",
						row -> makeExceptionLink(row.getExceptionRecordId())))
				.build(longRunningTaskList);
		body.with(div(header, description, taskTable));
		return body.with(
				br(),
				br(),
				span("Sent from: " + serverNameString));
	}

	private ATag makeTaskLink(String longRunningTaskName){
		String href = emailService.startLinkBuilder()
				.withLocalPath(taskPaths.datarouter.longRunningTasks)
				.withParam("name", longRunningTaskName)
				.withParam("status", "all")
				.build();
		return a(longRunningTaskName)
				.withHref(href);
	}

	private ATag makeExceptionLink(String exceptionRecordId){
		String href = emailService.startLinkBuilder()
				.withLocalPath(exceptionLink.getPath())
				.withParam(exceptionLink.getParamName(), exceptionRecordId)
				.build();
		return a(exceptionRecordId)
				.withHref(href);
	}

}
