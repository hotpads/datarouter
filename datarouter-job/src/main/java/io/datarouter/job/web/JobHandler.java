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
package io.datarouter.job.web;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.instrumentation.changelog.ChangelogRecorder.DatarouterChangelogDtoBuilder;
import io.datarouter.instrumentation.metric.MetricLinkBuilder;
import io.datarouter.job.BaseJob;
import io.datarouter.job.config.DatarouterJobFiles;
import io.datarouter.job.config.DatarouterJobPaths;
import io.datarouter.job.scheduler.JobCategoryTracker;
import io.datarouter.job.scheduler.JobPackage;
import io.datarouter.job.scheduler.JobScheduler;
import io.datarouter.job.service.JobStopperService;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.tasktracker.service.LongRunningTaskService;
import io.datarouter.tasktracker.service.LongRunningTaskService.LongRunningTaskSummaryDto;
import io.datarouter.tasktracker.service.LongRunningTaskTrackerFactory;
import io.datarouter.tasktracker.storage.LongRunningTask;
import io.datarouter.tasktracker.web.LongRunningTasksHandler;
import io.datarouter.types.MilliTime;
import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.time.DurationTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.InContextRedirectMav;
import io.datarouter.web.handler.mav.imp.MessageMav;
import io.datarouter.web.handler.mav.imp.StringMav;
import io.datarouter.web.handler.types.Param;
import io.datarouter.web.metriclinks.MetricLinkDto.LinkDto;
import jakarta.inject.Inject;

public class JobHandler extends BaseHandler{
	private static final Logger logger = LoggerFactory.getLogger(JobHandler.class);

	@Inject
	private DatarouterJobFiles files;
	@Inject
	private JobScheduler jobScheduler;
	@Inject
	private JobCategoryTracker jobCategoryTracker;
	@Inject
	private JobStopperService jobStopperService;
	@Inject
	private JobPackageFilter jobPackageFilter;
	@Inject
	private LongRunningTaskService longRunningTaskService;
	@Inject
	private DatarouterJobPaths datarouterJobPaths;
	@Inject
	private ChangelogRecorder changelogRecorder;
	@Inject
	private ServerName serverName;
	@Inject
	private MetricLinkBuilder linkBuilder;

	@Handler(defaultHandler = true)
	Mav defaultMethod(){
		return new InContextRedirectMav(request, datarouterJobPaths.datarouter.triggers.list.toSlashedString());
	}

	@Handler
	Mav list(
			@Param("category")
			Optional<String> jobCategoryName,
			Optional<String> keyword,
			Optional<Boolean> enabled,
			Optional<Boolean> disabled){
		Optional<String> messageOpt = params.optional("jobTriggerResponseMessage");
		Mav mav = new Mav(files.jsp.admin.datarouter.job.triggersJsp);
		messageOpt.ifPresent(message -> mav.put("message", message));
		boolean hideEnabled = enabled.orElse(false);
		boolean hideDisabled = disabled.orElse(false);
		mav.put("serverName", serverName.get());
		mav.put("categoryRows", getJobCategoryDtos(jobCategoryName));

		LongRunningTaskSummaryDto longRunningTaskSummary = longRunningTaskService.getSummary();
		AtomicInteger rowId = new AtomicInteger();
		List<TriggerJspDto> triggerRows = jobPackageFilter.streamMatches(
				jobCategoryName.orElse(""),
				keyword.orElse(""),
				hideEnabled,
				hideDisabled)
				.map(jobClass -> jobToTriggerJspDto(rowId.incrementAndGet(), jobClass, longRunningTaskSummary))
				.toList();
		mav.put("triggerRows", triggerRows);
		mav.put("legend", LongRunningTasksHandler.legend().renderFormatted());
		return mav;
	}

	@Handler
	StringMav run(String name, Optional<Boolean> detached){
		Class<? extends BaseJob> jobClass = BaseJob.parseClass(name);
		Date startTime = new Date();
		String triggeredBy = getSessionInfo().getRequiredSession().getUsername();
		var started = detached.isPresent()
				? jobScheduler.triggerManualJob(jobClass, triggeredBy, detached.get())
				: jobScheduler.triggerManualJob(jobClass, triggeredBy);
		String message;
		if(started.failed()){
			message = "Could not start " + jobClass.getSimpleName() + " reason=" + started.reason();
		}else{
			Duration elapsedTime = DurationTool.sinceDate(startTime);
			message = "Finished manual trigger of " + jobClass.getSimpleName() + " in " + DurationTool.toString(
					elapsedTime);
			changelogRecorder.record(new DatarouterChangelogDtoBuilder("Job", name, "run",
					getSessionInfo().getRequiredSession().getUsername()).build());
		}
		logger.warn(message);
		return new StringMav(message);
	}

	@Handler
	Mav interrupt(String name, String servers){
		Require.notBlank(name);
		List<String> serverNames = Optional.ofNullable(servers)
				.map(string -> string.split(","))
				.map(strings -> Scanner.of(strings)
						.include(StringTool::notEmpty)
						.list())
				.orElseGet(List::of);
		if(serverNames.isEmpty()){
			return new MessageMav("Selected job is not running.");
		}
		jobStopperService.requestStop(name, serverNames, getSessionInfo().findNonEmptyUsername().get());
		return new MessageMav("Stop/interrupt requested for " + name + ". Stopping may take a minute.");
	}

	private List<JobCategoryJspDto> getJobCategoryDtos(Optional<String> selectedJobCategory){
		return jobCategoryTracker.getJobCategoryNames().stream()
				.map(category -> new JobCategoryJspDto(category, isJobCategorySelected(category, selectedJobCategory)))
				.toList();
	}

	private static boolean isJobCategorySelected(String category, Optional<String> selectedCategory){
		return selectedCategory.isPresent() && category.equals(selectedCategory.get());
	}

	private TriggerJspDto jobToTriggerJspDto(
			int rowId,
			JobPackage jobPackage,
			LongRunningTaskSummaryDto longRunningTaskSummary){
		String taskName = LongRunningTaskTrackerFactory.taskNameForClass(jobPackage.jobClass);
		LongRunningTask currentlyRunningTask = longRunningTaskSummary.currentlyRunningTasks.get(taskName);
		String heartbeatStatus = Optional.ofNullable(currentlyRunningTask)
				.map(LongRunningTask::getHeartbeatStatus)
				.map(heartBeatStatus -> heartBeatStatus.status)
				.orElse(null);
		LongRunningTask lastFinishedTask = longRunningTaskSummary.lastCompletions.get(taskName);
		Set<String> servers = longRunningTaskSummary.runningOnServers.getOrDefault(taskName, new TreeSet<>());
		String serversCsv = String.join(",", servers);

		String nextTrigger = jobPackage.getCronExpression()
				.map(cronExpression -> cronExpression.getNextValidTimeAfter(new Date()))
				.map(MilliTime::of)
				.map(milliTime -> milliTime.format(getUserZoneId()))
				.orElse("");
		LinkDto metricLink = LinkDto.of("Datarouter job " + jobPackage.jobClass.getSimpleName());

		return new TriggerJspDto(
				rowId,
				jobPackage.jobClass.getName(),
				jobPackage.jobClass.getSimpleName(),
				jobPackage.shouldRunSupplier.get(),
				jobPackage.shouldRunDetached,
				heartbeatStatus,
				jobPackage.usesLocking() ? "locked" : "parallel",
				jobPackage.getCronExpressionString().orElse(""),
				nextTrigger,
				jobPackage.jobCategoryName,
				lastFinishedTask == null ? null : lastFinishedTask.getFinishTimeString(getUserZoneId()),
				lastFinishedTask == null ? -1 : lastFinishedTask.getFinish().toEpochMilli(),
				serversCsv,
				linkBuilder.availableMetricsLink(metricLink.metric));
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
		public final boolean detachedJob;
		public final String heartbeatStatus;
		public final String jobSchedule;
		public final String cronExpression;
		public final String nextExecution;
		public final String categoryName;
		public final String lastFinishTime;
		public final Long lastFinishSortableTime;
		public final String runningOnServers;
		public final String metricsLink;

		public TriggerJspDto(
				int rowId,
				String className,
				String classSimpleName,
				boolean shouldRun,
				boolean detachedJob,
				String status,
				String jobSchedule,
				String cronExpression,
				String nextExecution,
				String categoryName,
				String lastFinishTime,
				long lastFinishSortableTime,
				String runningOnServers,
				String metricsLink){
			this.rowId = rowId;
			this.className = className;
			this.classSimpleName = classSimpleName;
			this.shouldRun = shouldRun;
			this.detachedJob = detachedJob;
			this.heartbeatStatus = status;
			this.jobSchedule = jobSchedule;
			this.cronExpression = cronExpression;
			this.nextExecution = nextExecution;
			this.categoryName = categoryName;
			this.lastFinishTime = lastFinishTime;
			this.lastFinishSortableTime = lastFinishSortableTime;
			this.runningOnServers = runningOnServers;
			this.metricsLink = metricsLink;
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

		public boolean isDetachedJob(){
			return detachedJob;
		}

		public String getHeartbeatStatus(){
			return heartbeatStatus;
		}

		public String getJobSchedule(){
			return jobSchedule;
		}

		public String getCronExpression(){
			return cronExpression;
		}

		public String getNextExecution(){
			return nextExecution;
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

		public String getMetricsLink(){
			return metricsLink;
		}
	}

}
