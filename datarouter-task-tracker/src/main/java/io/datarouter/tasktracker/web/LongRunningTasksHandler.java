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
package io.datarouter.tasktracker.web;

import static j2html.TagCreator.div;

import java.time.Duration;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.datarouter.auth.service.CurrentUserSessionInfoService;
import io.datarouter.scanner.Scanner;
import io.datarouter.tasktracker.config.DatarouterTaskTrackerFiles;
import io.datarouter.tasktracker.scheduler.LongRunningTaskStatus;
import io.datarouter.tasktracker.storage.LongRunningTask;
import io.datarouter.tasktracker.storage.LongRunningTaskDao;
import io.datarouter.types.MilliTime;
import io.datarouter.util.duration.DatarouterDuration;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.web.exception.ExceptionLinkBuilder;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.J2HtmlLegendTable;
import j2html.tags.specialized.DivTag;
import jakarta.inject.Inject;

public class LongRunningTasksHandler extends BaseHandler{

	public static final String ALL_STATUSES_VALUE = "all";
	public static final String P_name = "name";
	public static final String P_status = "status";

	@Inject
	private LongRunningTaskDao longRunningTaskDao;
	@Inject
	private DatarouterTaskTrackerFiles files;
	@Inject
	private CurrentUserSessionInfoService currentUserSessionInfoService;
	@Inject
	private ExceptionLinkBuilder exceptionLinkBuilder;

	@Handler(defaultHandler = true)
	Mav longRunningTasks(Optional<String> name, Optional<String> status){
		Mav mav = new Mav(files.jsp.admin.datarouter.tasktracker.longRunningTasksJsp);
		String lowercaseNameSearch = name
				.map(String::toLowerCase)
				.map(String::trim)
				.orElse("");
		boolean showAllStatuses = status
				.map(ALL_STATUSES_VALUE::equals)
				.orElse(false);
		LongRunningTaskStatus filteredStatus;
		if(showAllStatuses){
			filteredStatus = null;
		}else{
			filteredStatus = status
					.map(LongRunningTaskStatus.BY_PERSISTENT_STRING::fromOrNull)
					.orElse(LongRunningTaskStatus.RUNNING);
		}
		ZoneId zoneId = currentUserSessionInfoService.getZoneId(request);
		List<LongRunningTaskJspDto> longRunningTasks = longRunningTaskDao.scan()
				.include(task -> task.getKey().getName().toLowerCase().contains(lowercaseNameSearch))
				.include(task -> showAllStatuses || task.getJobExecutionStatus() == filteredStatus)
				.map(task -> new LongRunningTaskJspDto(task, zoneId, exceptionLinkBuilder))
				.list();
		Set<TaskStatus> statuses = Scanner.of(LongRunningTaskStatus.values())
				.map(taskStatus -> new TaskStatus(taskStatus.name(), taskStatus.persistentString))
				.collect(HashSet::new);
		mav.put("longRunningTasks", longRunningTasks);
		mav.put("statuses", statuses);
		mav.put("allStatusesValue", ALL_STATUSES_VALUE);
		mav.put("displayedStatus", showAllStatuses ? ALL_STATUSES_VALUE : filteredStatus.persistentString);
		if(!showAllStatuses){
			mav.put("filteringStatusName", filteredStatus.name());
		}
		mav.put("nameSearch", name.orElse(""));
		mav.put("legend", legend().renderFormatted());
		return mav;
	}

	public static DivTag legend(){
		var table = new J2HtmlLegendTable()
				.withHeader("Legend")
				.withClass("table table-sm my-4 border")
				.withEntry("Running job", "last heartbeat within 2 seconds", "table-success")
				.withEntry("Running job", "last heartbeat within 2-10 seconds", "table-warning")
				.withEntry("Running job", "last heartbeat over 10 seconds", "table-danger")
				.build();
		return div(table)
				.withClass("container mt-5");
	}

	public record TaskStatus(
			String name,
			String value){

		// used in jsps
		public String getLeft(){
			return name();
		}

		// used in jsps
		public String getRight(){
			return value();
		}

	}

	public static class LongRunningTaskJspDto{

		private final String status;
		private final String heartbeatStatus;
		private final String name;
		private final String serverName;
		private final MilliTime triggerTime;
		private final MilliTime startTime;
		private final Duration duration;
		private final String durationString;
		private final MilliTime lastHeartbeat;
		private final String lastHeartbeatString;
		private final String lastItemProcessed;
		private final Long numItemsProcessed;
		private final MilliTime finishTime;
		private final String triggeredBy;
		private final String exceptionRecordId;
		private final ZoneId zoneId;
		private final ExceptionLinkBuilder exceptionLinkBuilder;

		public LongRunningTaskJspDto(
				LongRunningTask task,
				ZoneId zoneId,
				ExceptionLinkBuilder exceptionLinkBuilder){
			this.status = task.getJobExecutionStatus().persistentString;
			this.heartbeatStatus = task.getHeartbeatStatus() == null
					? null
					: task.getHeartbeatStatus().status;
			this.name = task.getKey().getName();
			this.serverName = task.getKey().getServerName();
			this.triggerTime = task.getKey().getTriggerTime();
			this.startTime = task.getStart();
			this.duration = task.getDuration();
			this.durationString = task.getDurationString();
			this.lastHeartbeat = task.getHeartbeat();
			this.lastHeartbeatString = task.getLastHeartbeatString(zoneId);
			this.lastItemProcessed = task.getLastItemProcessed();
			this.numItemsProcessed = task.getNumItemsProcessed();
			this.finishTime = task.getFinish();
			this.triggeredBy = task.getTriggeredBy();
			this.exceptionRecordId = task.getExceptionRecordId();
			this.zoneId = zoneId;
			this.exceptionLinkBuilder = exceptionLinkBuilder;
		}

		public String getStatus(){
			return status;
		}

		public String getHrefForTasksWithSameName(){
			return "?name=" + name + "&status=" + ALL_STATUSES_VALUE;
		}

		public String getHeartbeatStatus(){
			return heartbeatStatus;
		}

		public String getName(){
			return name;
		}

		public String getServerName(){
			return serverName;
		}

		public long getStartTime(){
			return startTime != null ? startTime.toEpochMilli() : triggerTime.toEpochMilli();
		}

		public String getStartString(){
			if(startTime == null){
				return triggerTime.format(zoneId) + " (trigger)";
			}else{
				return startTime.format(zoneId);
			}
		}

		public String getFinishTime(){
			if(finishTime == null){
				return "";
			}
			return finishTime.format(zoneId);
		}

		public Long getSortableDuration(){
			return duration != null ? duration.toMillis() : -1;
		}

		public String getDurationString(){
			return durationString;
		}

		public String getLastHeartbeat(){
			return Optional.ofNullable(lastHeartbeat)
					.map(time -> DatarouterDuration.ageMs(time.toEpochMilli()))
					.map(DatarouterDuration::toString)
					.map(str -> str + " ago")
					.orElse("");
		}

		public Long getSortableLastHeartbeat(){
			return lastHeartbeat != null ? lastHeartbeat.toEpochMilli() : -1;
		}

		public String getLastHeartbeatString(){
			return lastHeartbeatString;
		}

		public String getLastItemProcessed(){
			return lastItemProcessed;
		}

		public String getNumItemsProcessed(){
			return NumberFormatter.addCommas(numItemsProcessed);
		}

		public Long getSortableFinishTime(){
			return finishTime != null ? finishTime.toEpochMilli() : -1;
		}

		public String getFinishTimeString(){
			return Optional.ofNullable(finishTime)
					.map(time -> DatarouterDuration.ageMs(time.toEpochMilli()))
					.map(DatarouterDuration::toString)
					.map(str -> str + " ago")
					.orElse("");
		}

		public String getStartSubtitle(){
			String startTimeAgo = Optional.ofNullable(startTime)
					.map(time -> DatarouterDuration.ageMs(time.toEpochMilli()))
					.map(DatarouterDuration::toString)
					.map(str -> str + " ago\n")
					.orElse("");
			return startTimeAgo
					+ "Trigger " + triggerTime.format(zoneId) + "\n"
					+ "Trigger " + DatarouterDuration.ageMs(triggerTime.toEpochMilli()) + " ago";
		}

		public String getTriggeredBy(){
			return triggeredBy;
		}

		public String getExceptionRecordId(){
			return exceptionRecordId;
		}

		public String getHrefForException(){
			return exceptionLinkBuilder.exception(exceptionRecordId).orElseThrow();
		}

	}

}
