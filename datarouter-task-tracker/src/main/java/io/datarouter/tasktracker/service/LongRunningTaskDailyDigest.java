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

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.instrumentation.relay.rml.Rml;
import io.datarouter.instrumentation.relay.rml.RmlBlock;
import io.datarouter.tasktracker.config.DatarouterTaskTrackerPaths;
import io.datarouter.tasktracker.storage.LongRunningTask;
import io.datarouter.tasktracker.storage.LongRunningTaskDao;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestRmlService;
import io.datarouter.web.exception.ExceptionLinkBuilder;
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
	private DailyDigestRmlService digestService;
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

	@Override
	public List<DailyDigestPlatformTask> getTasks(ZoneId zoneId){
		return List.of();
	}

	private String makeTaskHref(String longRunningTaskName){
		return emailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.longRunningTasks)
				.withParam("name", longRunningTaskName)
				.withParam("status", "all")
				.build();
	}

}
