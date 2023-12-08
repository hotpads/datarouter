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

import io.datarouter.email.html.J2HtmlEmailTable;
import io.datarouter.email.html.J2HtmlEmailTable.J2HtmlEmailTableColumn;
import io.datarouter.tasktracker.config.DatarouterTaskTrackerPaths;
import io.datarouter.tasktracker.storage.LongRunningTask;
import io.datarouter.tasktracker.storage.LongRunningTaskDao;
import io.datarouter.tasktracker.web.TaskTrackerExceptionLink;
import io.datarouter.web.config.ServletContextSupplier;
import io.datarouter.web.config.service.DomainFinder;
import io.datarouter.web.digest.DailyDigest;
import io.datarouter.web.digest.DailyDigestGrouping;
import io.datarouter.web.digest.DailyDigestService;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.TableTag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class LongRunningTaskDailyDigest implements DailyDigest{

	@Inject
	private LongRunningTaskDao longRunningTaskDao;
	@Inject
	private TaskTrackerExceptionLink exceptionLink;
	@Inject
	private DatarouterHtmlEmailService emailService;
	@Inject
	private DatarouterTaskTrackerPaths paths;
	@Inject
	private ServletContextSupplier contextSupplier;
	@Inject
	private DailyDigestService digestService;
	@Inject
	private DomainFinder domainFinder;

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
	public String getTitle(){
		return "Long Running Tasks";
	}

	@Override
	public DailyDigestGrouping getGrouping(){
		return DailyDigestGrouping.HIGH;
	}

	@Override
	public DailyDigestType getType(){
		return DailyDigestType.SUMMARY;
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
		String href = emailService.startLinkBuilder()
				.withLocalPath(paths.datarouter.longRunningTasks)
				.withParam("name", longRunningTaskName)
				.withParam("status", "all")
				.build();
		return a(longRunningTaskName)
				.withHref(href);
	}

	private ATag makeExceptionLink(String exceptionRecordId){
		String href = "https://" + domainFinder.getDomainPreferPublic() + contextSupplier.get().getContextPath()
				+ exceptionLink.buildExceptionDetailLink(exceptionRecordId);
		return a(exceptionRecordId)
				.withHref(href);
	}

}
