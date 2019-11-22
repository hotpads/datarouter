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
package io.datarouter.job.web;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.job.BaseJob;
import io.datarouter.job.config.DatarouterJobFiles;
import io.datarouter.job.config.DatarouterJobPaths;
import io.datarouter.job.lock.LocalTriggerLockService;
import io.datarouter.job.scheduler.JobCategoryTracker;
import io.datarouter.job.scheduler.JobPackage;
import io.datarouter.job.scheduler.JobScheduler;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.tasktracker.service.LongRunningTaskService;
import io.datarouter.tasktracker.service.LongRunningTaskService.LongRunningTaskSummaryDto;
import io.datarouter.tasktracker.service.LongRunningTaskTrackerFactory;
import io.datarouter.tasktracker.storage.LongRunningTask;
import io.datarouter.util.collection.SetTool;
import io.datarouter.util.time.DurationTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.types.optional.OptionalBoolean;
import io.datarouter.web.handler.types.optional.OptionalString;
import io.datarouter.web.user.session.CurrentUserSessionInfo;

public class JobHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(JobHandler.class);

	@Inject
	private DatarouterJobFiles files;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private JobScheduler jobScheduler;
	@Inject
	private JobCategoryTracker jobCategoryTracker;
	@Inject
	private LocalTriggerLockService localTriggerLockService;
	@Inject
	private JobPackageFilter jobPackageFilter;
	@Inject
	private LongRunningTaskService longRunningTaskService;
	@Inject
	private CurrentUserSessionInfo currentUserSessionInfo;
	@Inject
	private DatarouterJobPaths datarouterJobPaths;

	@Handler(defaultHandler = true)
	Mav defaultMethod(){
		return new InContextRedirectMav(request, datarouterJobPaths.datarouter.triggers.list.toSlashedString());
	}

	@Handler
	Mav list(OptionalString category, OptionalString keyword, OptionalBoolean enabled, OptionalBoolean disabled){
		Optional<String> message = params.optional("jobTriggerResponseMessage");
		Mav mav = new Mav(files.jsp.admin.datarouter.job.triggersJsp);
		if(message.isPresent()){
			mav.put("message", message.get());
		}
		Optional<String> jobCategoryName = category.getOptional();
		boolean hideEnabled = enabled.orElse(false);
		boolean hideDisabled = disabled.orElse(false);
		mav.put("serverName", datarouterProperties.getServerName());
		mav.put("categoryRows", getJobCategoryDtos(jobCategoryName));

		LongRunningTaskSummaryDto longRunningTaskSummary = longRunningTaskService.getSummary();
		AtomicInteger rowId = new AtomicInteger();
		List<TriggerJspDto> triggerRows = jobPackageFilter.streamMatches(jobCategoryName.orElse(""),
				keyword.orElse(""), hideEnabled, hideDisabled)
				.map(jobClass -> jobToTriggerJspDto(rowId.incrementAndGet(), jobClass, longRunningTaskSummary))
				.collect(Collectors.toList());
		mav.put("triggerRows", triggerRows);
		return mav;
	}

	@Handler
	Mav run(String name){
		Class<? extends BaseJob> jobClass = BaseJob.parseClass(name);
		Map<String,Object> jobTriggerResponse = new HashMap<>();
		Date startTime = new Date();
		String triggeredBy = currentUserSessionInfo.getRequiredSession(request).getUsername();
		boolean started = jobScheduler.triggerManualJob(jobClass, triggeredBy);
		if(!started){
			String message = "triggerManualJob failed for " + jobClass.getSimpleName();
			logger.warn(message);
			jobTriggerResponse.put("jobTriggerResponseMessage", message);
			return new InContextRedirectMav(request, datarouterJobPaths.datarouter.triggers.list.toSlashedString(),
					jobTriggerResponse);
		}
		Duration elapsedTime = DurationTool.sinceDate(startTime);
		String message = "Finished manual trigger of " + jobClass.getSimpleName() + " in " + DurationTool.toString(
				elapsedTime);
		logger.warn(message);
		jobTriggerResponse.put("jobTriggerResponseMessage", message);
		return new InContextRedirectMav(request, datarouterJobPaths.datarouter.triggers.list.toSlashedString(),
				jobTriggerResponse);
	}

	@Handler
	Mav interrupt(String name){
		Class<? extends BaseJob> jobClass = BaseJob.parseClass(name);
		localTriggerLockService.getForClass(jobClass).requestStop();
		return new MessageMav("requested stop for " + name);
	}

	private List<JobCategoryJspDto> getJobCategoryDtos(Optional<String> selectedJobCategory){
		return jobCategoryTracker.getJobCategoryNames().stream()
				.map(category -> new JobCategoryJspDto(category, isJobCategorySelected(category, selectedJobCategory)))
				.collect(Collectors.toList());
	}

	private static boolean isJobCategorySelected(String category, Optional<String> selectedCategory){
		return selectedCategory.isPresent() && category.equals(selectedCategory.get());
	}

	private TriggerJspDto jobToTriggerJspDto(int rowId, JobPackage jobPackage,
			LongRunningTaskSummaryDto longRunningTaskSummary){
		String taskName = LongRunningTaskTrackerFactory.taskNameForClass(jobPackage.jobClass);
		LongRunningTask currentlyRunningTask = longRunningTaskSummary.currentlyRunningTasks.get(taskName);
		String heartbeatStatus = currentlyRunningTask == null ? null : currentlyRunningTask.getHeartbeatStatus();
		LongRunningTask lastFinishedTask = longRunningTaskSummary.lastCompletions.get(taskName);
		Set<String> servers = SetTool.nullsafe(longRunningTaskSummary.runningOnServers.get(taskName));
		String serversCsv = String.join(",", servers);
		return new TriggerJspDto(
				rowId,
				jobPackage.jobClass.getName(),
				jobPackage.jobClass.getSimpleName(),
				jobPackage.shouldRunSupplier.get(),
				heartbeatStatus,
				jobPackage.getCronExpressionString().orElse(""),
				jobPackage.jobCategoryName,
				lastFinishedTask == null ? null : lastFinishedTask.getFinishTimeString(),
				lastFinishedTask == null ? -1 : lastFinishedTask.getFinishTime().getTime(),
				serversCsv);
	}

	public static class JobCategoryJspDto{
		private final String name;
		private final boolean selected;

		public JobCategoryJspDto(String name, boolean selected){
			this.name = name;
			this.selected = selected;
		}

		public String getName(){
			return name;
		}

		public boolean getSelected(){
			return selected;
		}
	}

	public static class TriggerJspDto{
		public final int rowId;
		public final String className;
		public final String classSimpleName;
		public final boolean shouldRun;
		public final String heartbeatStatus;
		public final String cronExpression;
		public final String categoryName;
		public final String lastFinishTime;
		public final Long lastFinishSortableTime;
		public final String runningOnServers;

		public TriggerJspDto(int rowId, String className, String classSimpleName, boolean shouldRun, String status,
				String cronExpression, String categoryName, String lastFinishTime, long lastFinishSortableTime,
				String runningOnServers){
			this.rowId = rowId;
			this.className = className;
			this.classSimpleName = classSimpleName;
			this.shouldRun = shouldRun;
			this.heartbeatStatus = status;
			this.cronExpression = cronExpression;
			this.categoryName = categoryName;
			this.lastFinishTime = lastFinishTime;
			this.lastFinishSortableTime = lastFinishSortableTime;
			this.runningOnServers = runningOnServers;
		}

		public int getRowId(){
			return rowId;
		}

		public String getClassName(){
			return className;
		}

		public String getClassSimpleName(){
			return classSimpleName;
		}

		public boolean isShouldRun(){
			return shouldRun;
		}

		public String getHeartbeatStatus(){
			return heartbeatStatus;
		}

		public String getCronExpression(){
			return cronExpression;
		}

		public String getCategoryName(){
			return categoryName;
		}

		public String getLastFinishTime(){
			return lastFinishTime;
		}

		public Long getLastFinishSortableTime(){
			return lastFinishSortableTime;
		}

		public String getRunningOnServers(){
			return runningOnServers;
		}
	}

}