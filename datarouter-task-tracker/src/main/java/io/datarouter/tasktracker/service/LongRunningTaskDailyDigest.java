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
package io.datarouter.tasktracker.service;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.small;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.tasktracker.config.DatarouterTaskTrackerPaths;
import io.datarouter.tasktracker.storage.LongRunningTask;
import io.datarouter.tasktracker.storage.LongRunningTaskDao;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.exception.ExceptionLinkBuilder;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class LongRunningTaskDailyDigest implements DailyDigest{

	private static final Duration TOO_OLD_TRIGGER_TIME = Duration.ofDays(1);

	@Inject
	private LongRunningTaskDao longRunningTaskDao;
	@Inject
	private DatarouterHtmlEmailService emailService;
	@Inject
	private DatarouterTaskTrackerPaths paths;
	@Inject
	private DailyDigestService digestService;
	@Inject
	private ExceptionLinkBuilder exceptionLinkBuilder;

	@Override
	public String getTitle(){
		return "Long Running Tasks";
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.SUMMARY;
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.HIGH;
	}

	@Override
	public Optional<DivTag> getEmailContent(ZoneId zoneId){
		List<LongRunningTask> failedTasks = longRunningTaskDao.scan()
				.include(task -> task.getKey().getTriggerTime().toEpochMilli()
						> System.currentTimeMillis() - Duration.ofDays(1).toMillis())
				.include(LongRunningTask::isBadState)
				.list();
		if(failedTasks.isEmpty()){
			return Optional.empty();
		}
		var header = digestService.makeHeader("Failed Long Running Tasks", paths.datarouter.longRunningTasks);
		var description = small("From the last 24 hours");
		var table = buildEmailTable(failedTasks, zoneId);
		return Optional.of(div(header, description, table));
	}

	@Override
	public Optional<RmlBlock> getRelayContent(ZoneId zoneId){
		List<LongRunningTask> failedTasks = longRunningTaskDao.scan()
				.exclude(task -> task.getKey().getTriggerTime().isOlderThan(TOO_OLD_TRIGGER_TIME))
				.include(LongRunningTask::isBadState)
				.list();
		if(failedTasks.isEmpty()){
			return Optional.empty();
		}
		return Optional.of(Rml.paragraph(
				digestService.makeHeading("Failed Long Running Tasks", paths.datarouter.longRunningTasks),
				Rml.text("From the last 24 hours").italic(),
				Rml.table(
						Rml.tableRow(
								Rml.tableHeader(Rml.text("Name")),
								Rml.tableHeader(Rml.text("Trigger Time")),
								Rml.tableHeader(Rml.text("Duration")),
								Rml.tableHeader(Rml.text("Triggered By")),
								Rml.tableHeader(Rml.text("Status")),
								Rml.tableHeader(Rml.text("Exception"))))
						.with(failedTasks.stream()
								.map(task -> Rml.tableRow(
										Rml.tableCell(Rml.text(task.getKey().getName())
												.link(makeTaskHref(task.getKey().getName()))),
										Rml.tableCell(Rml.timestamp(task.getKey().getTriggerTime().format(zoneId),
												task.getKey().getTriggerTime().toEpochMilli())),
										Rml.tableCell(Rml.text(task.getDurationString())),
										Rml.tableCell(Rml.text(task.getTriggeredBy())),
										Rml.tableCell(Rml.text(task.getJobExecutionStatus().persistentString)),
										Rml.tableCell(Rml.text(task.getExceptionRecordId())
												.link(exceptionLinkBuilder.exception(task.getExceptionRecordId())
														.orElseThrow())))))));
	}

	private TableTag buildEmailTable(List<LongRunningTask> rows, ZoneId zoneId){
		return new J2HtmlEmailTable<LongRunningTask>()
				.withColumn(new J2HtmlEmailTableColumn<>("Name", row -> makeTaskLink(row.getKey().getName())))
				.withColumn("Trigger Time", row -> row.getKey().getTriggerTime().format(zoneId))
				.withColumn("Duration", LongRunningTask::getDurationString)
				.withColumn("Triggered By", LongRunningTask::getTriggeredBy)
				.withColumn("Status", row -> row.getJobExecutionStatus().persistentString)
				.withColumn(new J2HtmlEmailTableColumn<>("Exception",
						row -> makeExceptionLink(row.getExceptionRecordId())))
				.build(rows);
	}

	private ATag makeTaskLink(String longRunningTaskName){
		return a(longRunningTaskName)
				.withHref(makeTaskHref(longRunningTaskName));
	}

	private String makeTaskHref(String longRunningTaskName){
		return emailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.longRunningTasks)
				.withParam("name", longRunningTaskName)
				.withParam("status", "all")
				.build();
	}

	private ATag makeExceptionLink(String exceptionRecordId){
		String href = exceptionLinkBuilder.exception(exceptionRecordId).orElseThrow();
		return a(exceptionRecordId)
				.withHref(href);
	}

}
