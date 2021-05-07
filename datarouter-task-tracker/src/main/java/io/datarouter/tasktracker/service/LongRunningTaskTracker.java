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
import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.p;
import static j2html.TagCreator.text;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.instrumentation.task.TaskStatus;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.storage.setting.Setting;
import io.datarouter.tasktracker.TaskTrackerCounters;
import io.datarouter.tasktracker.config.DatarouterTaskTrackerPaths;
import io.datarouter.tasktracker.scheduler.LongRunningTaskStatus;
import io.datarouter.tasktracker.storage.LongRunningTask;
import io.datarouter.tasktracker.storage.LongRunningTaskKey;
import io.datarouter.tasktracker.web.LongRunningTaskGraphLink;
import io.datarouter.tasktracker.web.LongRunningTasksHandler;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.mutable.MutableBoolean;
import j2html.tags.ContainerTag;

public class LongRunningTaskTracker implements TaskTracker{
	private static final Logger logger = LoggerFactory.getLogger(LongRunningTaskTracker.class);

	private static final Duration HEARTBEAT_PERSIST_PERIOD = Duration.ofSeconds(1);

	private final DatarouterTaskTrackerPaths datarouterTaskTrackerPaths;
	private final DatarouterHtmlEmailService datarouterHtmlEmailService;
	private final DatarouterProperties datarouterProperties;
	private final DatarouterAdministratorEmailService datarouterAdministratorEmailService;
	private final LongRunningTaskGraphLink longRunningTaskGraphLink;
	private final Setting<Boolean> persistSetting;
	private final SortedMapStorage<LongRunningTaskKey,LongRunningTask> node;
	private final TaskTrackerCounters counters;
	private final ServerTypeDetector serverTypeDetector;

	private final LongRunningTaskInfo task;
	private final Optional<Instant> deadline;
	private final boolean warnOnReachingInterrupt;
	private final MutableBoolean stopRequested;
	private volatile Instant lastReported;
	private volatile boolean deadlineAlertAttempted;
	private final Consumer<LongRunningTaskTracker> callback;

	public LongRunningTaskTracker(
			DatarouterTaskTrackerPaths datarouterTaskTrackerPaths,
			DatarouterHtmlEmailService datarouterHtmlEmailService,
			DatarouterProperties datarouterProperties,
			DatarouterAdministratorEmailService datarouterAdministratorEmailService,
			LongRunningTaskGraphLink longRunningTaskGraphLink,
			Setting<Boolean> persistSetting,
			SortedMapStorage<LongRunningTaskKey,LongRunningTask> node,
			TaskTrackerCounters counters,
			ServerTypeDetector serverTypeDetector,
			LongRunningTaskInfo task,
			Instant deadline,
			boolean warnOnReachingInterrupt,
			Consumer<LongRunningTaskTracker> callback){
		this.datarouterTaskTrackerPaths = datarouterTaskTrackerPaths;
		this.datarouterHtmlEmailService = datarouterHtmlEmailService;
		this.datarouterProperties = datarouterProperties;
		this.datarouterAdministratorEmailService = datarouterAdministratorEmailService;
		this.longRunningTaskGraphLink = longRunningTaskGraphLink;
		this.persistSetting = persistSetting;
		this.node = node;
		this.counters = counters;
		this.serverTypeDetector = serverTypeDetector;

		this.task = task;
		this.deadline = Optional.ofNullable(deadline);
		this.warnOnReachingInterrupt = warnOnReachingInterrupt;
		this.stopRequested = new MutableBoolean(false);
		this.deadlineAlertAttempted = false;
		this.callback = callback;
	}

	public LongRunningTaskTracker(
			DatarouterTaskTrackerPaths datarouterTaskTrackerPaths,
			DatarouterHtmlEmailService datarouterHtmlEmailService,
			DatarouterProperties datarouterProperties,
			DatarouterAdministratorEmailService datarouterAdministratorEmailService,
			LongRunningTaskGraphLink longRunningTaskGraphLink,
			Setting<Boolean> persistSetting,
			SortedMapStorage<LongRunningTaskKey,LongRunningTask> node,
			TaskTrackerCounters counters,
			ServerTypeDetector serverTypeDetector,
			LongRunningTaskInfo task,
			Instant deadline,
			boolean warnOnReachingInterrupt){
		this(datarouterTaskTrackerPaths,
				datarouterHtmlEmailService,
				datarouterProperties,
				datarouterAdministratorEmailService,
				longRunningTaskGraphLink,
				persistSetting,
				node,
				counters,
				serverTypeDetector,
				task,
				deadline,
				warnOnReachingInterrupt,
				$ -> {});
	}

	@Override
	public String getName(){
		return task.name;
	}

	@Override
	public String getServerName(){
		return task.serverName;
	}

	/*----------- timing ----------------*/

	@Override
	public TaskTracker setScheduledTime(Instant scheduledTime){
		task.triggerTime = Date.from(scheduledTime);
		return this;
	}

	@Override
	public Instant getScheduledTime(){
		return task.triggerTime.toInstant();
	}

	@Override
	public TaskTracker onStart(){
		task.startTime = Date.from(Instant.now());
		return this;
	}

	@Override
	public TaskTracker setStartTime(Instant instant){
		task.startTime = Date.from(instant);
		return this;
	}

	@Override
	public Instant getStartTime(){
		return task.startTime.toInstant();
	}

	@Override
	public TaskTracker onFinish(){
		task.finishTime = new Date();
		return this;
	}

	@Override
	public TaskTracker setFinishTime(Instant instant){
		task.finishTime = Date.from(instant);
		return this;
	}

	@Override
	public Instant getFinishTime(){
		return task.finishTime.toInstant();
	}

	/*------------ counting ---------------*/

	@Override
	public long getCount(){
		return task.numItemsProcessed;
	}

	@Override
	public String getLastItem(){
		return task.lastItemProcessed;
	}

	/*--------------- heartbeat --------------*/

	@Override
	public LongRunningTaskTracker increment(){
		return increment(1);
	}

	@Override
	public LongRunningTaskTracker increment(long delta){
		counters.increment(task.name, delta);
		task.numItemsProcessed += delta;
		return heartbeat();
	}

	@Override
	public LongRunningTaskTracker heartbeat(long numItemsProcessed){
		task.numItemsProcessed = numItemsProcessed;
		return heartbeat();
	}

	@Override
	public LongRunningTaskTracker heartbeat(){
		counters.heartbeat(task.name);
		task.heartbeatTime = new Date();
		reportIfEnoughTimeElapsed();
		return this;
	}

	@Override
	public LongRunningTaskTracker setLastItemProcessed(String lastItemProcessed){
		task.lastItemProcessed = lastItemProcessed;
		return this;
	}

	/*------------- status ----------------*/

	@Override
	public TaskTracker setStatus(TaskStatus status){
		task.longRunningTaskStatus = LongRunningTaskStatus.fromTaskStatus(status);
		return this;
	}

	@Override
	public TaskStatus getStatus(){
		return task.longRunningTaskStatus.getStatus();
	}

	/*------------ interrupt -----------------*/

	@Override
	public TaskTracker requestStop(){
		logger.warn("requestStop on " + task.name);
		stopRequested.set(true);
		return this;
	}

	@Override
	public boolean shouldStop(){
		heartbeat();
		if(stopRequested.get()){
			task.longRunningTaskStatus = LongRunningTaskStatus.STOP_REQUESTED;
			onShouldStop("stop requested");
			return true;
		}
		if(deadline.map(instant -> Instant.now().isAfter(instant)).orElse(false)){
			task.longRunningTaskStatus = LongRunningTaskStatus.MAX_DURATION_REACHED;
			sendMaxDurationAlertIfShould();
			onShouldStop("maxDuration reached");
			return true;
		}
		if(Thread.currentThread().isInterrupted()){
			task.longRunningTaskStatus = LongRunningTaskStatus.INTERRUPTED;
			onShouldStop("thread interrupted");
			return true;
		}
		return false;
	}

	private void onShouldStop(String reason){
		counters.shouldStop(task.name, reason);
		logger.warn("{} shouldStop because {}", task.name, reason);
		doReportTasks();
	}

	private void sendMaxDurationAlertIfShould(){
		if(!warnOnReachingInterrupt){
			return;
		}
		if(deadlineAlertAttempted){
			return;
		}
		deadlineAlertAttempted = true;
		String from = datarouterProperties.getAdministratorEmail();
		String to;
		if(serverTypeDetector.mightBeDevelopment()){
			to = datarouterProperties.getAdministratorEmail();
		}else{
			to = datarouterAdministratorEmailService.getAdministratorEmailAddressesCsv();
		}
		String primaryHref = datarouterHtmlEmailService.startLinkBuilder()
				.withLocalPath(datarouterTaskTrackerPaths.datarouter.longRunningTasks)
				.withParam(LongRunningTasksHandler.P_name, task.name)
				.withParam(LongRunningTasksHandler.P_status, LongRunningTasksHandler.ALL_STATUSES_VALUE)
				.build();
		var emailBuilder = datarouterHtmlEmailService.startEmailBuilder()
				.withTitle("Task Timeout")
				.withTitleHref(primaryHref)
				.withContent(makeEmailBody(task.name, datarouterProperties.getServerName(), primaryHref));
		datarouterHtmlEmailService.trySendJ2Html(from, to, emailBuilder);
	}

	private ContainerTag makeEmailBody(String name, String serverName, String detailsHref){
		var message = p(
				a("Deadline reached for " + name).withHref(detailsHref),
				text(String.format(" on %s. Consider extending the trigger period.", serverName)));
		var tasksLink = a("Tasks").withHref(detailsHref);
		var counterLink = a("Counters").withHref(longRunningTaskGraphLink.getLink(name));
		return body(message, br(), tasksLink, br(), counterLink);
	}

	/*------------ persist -----------------*/

	private void reportIfEnoughTimeElapsed(){
		if(lastReported == null
				|| ComparableTool.gt(Duration.between(lastReported, Instant.now()), HEARTBEAT_PERSIST_PERIOD)){
			doReportTasks();
		}
	}

	public void doReportTasks(){
		if(task.triggerTime == null){
			logger.warn("setting null triggerTime to now on {}", task.databeanName);
			task.triggerTime = new Date();
		}
		reportCallback();
		persist();
		lastReported = Instant.now();
	}

	private void reportCallback(){
		try{
			callback.accept(this);
		}catch(Exception e){
			logger.warn("Unable to report taskTracker to callback for {}", task.name, e);
		}
	}

	/*------------ exception -----------------*/

	public void setExceptionRecordId(String exceptionRecordId){
		task.exceptionRecordId = exceptionRecordId;
		persist();
	}

	private void persist(){
		if(node != null && persistSetting != null && persistSetting.get()){
			node.put(new LongRunningTask(task));
		}
	}

}
