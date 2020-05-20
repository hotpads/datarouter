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
package io.datarouter.tasktracker.web;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.tasktracker.config.DatarouterTaskTrackerFiles;
import io.datarouter.tasktracker.scheduler.LongRunningTaskStatus;
import io.datarouter.tasktracker.storage.DatarouterLongRunningTaskDao;
import io.datarouter.tasktracker.storage.LongRunningTask;
import io.datarouter.util.DateTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.html.j2html.J2HtmlLegendTable;

public class LongRunningTasksHandler extends BaseHandler{

	public static final String ALL_STATUSES_VALUE = "all";
	public static final String P_name = "name";
	public static final String P_status = "status";

	@Inject
	private DatarouterLongRunningTaskDao longRunningTaskDao;
	@Inject
	private DatarouterTaskTrackerFiles files;

	@Handler(defaultHandler = true)
	Mav longRunningTasks(OptionalString name, OptionalString status){
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
					.map(LongRunningTaskStatus::fromPersistentStringStatic)
					.orElse(LongRunningTaskStatus.RUNNING);
		}
		List<LongRunningTaskJspDto> longRunningTasks = longRunningTaskDao.scan()
				.include(task -> task.getKey().getName().toLowerCase().contains(lowercaseNameSearch))
				.include(task -> showAllStatuses || task.getJobExecutionStatus() == filteredStatus)
				.map(LongRunningTaskJspDto::new)
				.list();
		Set<Pair<String,String>> statuses = Arrays.stream(LongRunningTaskStatus.values())
				.map(jobExecutionStatus -> new Pair<>(jobExecutionStatus.name(), jobExecutionStatus
						.getPersistentString()))
				.collect(Collectors.toSet());
		mav.put("longRunningTasks", longRunningTasks);
		mav.put("statuses", statuses);
		mav.put("allStatusesValue", ALL_STATUSES_VALUE);
		mav.put("displayedStatus", showAllStatuses ? ALL_STATUSES_VALUE : filteredStatus.getPersistentString());
		if(!showAllStatuses){
			mav.put("filteringStatusName", filteredStatus.name());
		}
		mav.put("nameSearch", name.orElse(""));
		mav.put("legend", legend());
		return mav;
	}

	public static String legend(){
		return new J2HtmlLegendTable()
				.withHeader("Legend")
				.withClass("table table-sm my-4 border")
				.withEntry("Running job", "last heartbeat within 2 seconds", "table-success")
				.withEntry("Running job", "last heartbeat within 2-10 seconds", "table-warning")
				.withEntry("Running job", "last heartbeat over 10 seconds", "table-danger")
				.build()
				.renderFormatted();
	}

	public static class LongRunningTaskJspDto{

		private final String status;
		private final String heartbeatStatus;
		private final String name;
		private final String serverName;
		private final Date startTime;
		private final Duration duration;
		private final String durationString;
		private final Date lastHeartbeat;
		private final String lastHeartbeatString;
		private final String lastItemProcessed;
		private final Long numItemsProcessed;
		private final Date finishTime;
		private final String finishTimeString;
		private final String triggeredBy;

		public LongRunningTaskJspDto(LongRunningTask task){
			this.status = task.getJobExecutionStatus().getPersistentString();
			this.heartbeatStatus = task.getHeartbeatStatus();
			this.name = task.getKey().getName();
			this.serverName = task.getKey().getServerName();
			this.startTime = task.getStartTime();
			this.duration = task.getDuration();
			this.durationString = task.getDurationString();
			this.lastHeartbeat = task.getHeartbeatTime();
			this.lastHeartbeatString = task.getLastHeartbeatString();
			this.lastItemProcessed = task.getLastItemProcessed();
			this.numItemsProcessed = task.getNumItemsProcessed();
			this.finishTime = task.getFinishTime();
			this.finishTimeString = task.getFinishTimeString();
			this.triggeredBy = task.getTriggeredBy();
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

		public Date getStartTime(){
			return startTime;
		}

		public Date getFinishTime(){
			return finishTime;
		}

		public Long getSortableDuration(){
			return duration != null ? duration.toMillis() : -1;
		}

		public String getDurationString(){
			return durationString;
		}

		public Long getSortableLastHeartbeat(){
			return lastHeartbeat != null ? lastHeartbeat.getTime() : -1;
		}

		public String getLastHeartbeatString(){
			return lastHeartbeatString;
		}

		public String getLastItemProcessed(){
			return lastItemProcessed;
		}

		public Long getNumItemsProcessed(){
			return numItemsProcessed;
		}

		public Long getSortableFinishTime(){
			return finishTime != null ? finishTime.getTime() : -1;
		}

		public String getFinishTimeString(){
			return Optional.ofNullable(finishTimeString)
					.filter(StringTool::notEmpty)
					.map("Finished "::concat)
					.orElse(null);
		}

		public String getStartTimeString(){
			return Optional.ofNullable(startTime)
					.map(Date::toInstant)
					.map(DateTool::getAgoString)
					.map("Started "::concat)
					.orElse(null);
		}

		public String getTriggeredBy(){
			return triggeredBy;
		}

	}

}
