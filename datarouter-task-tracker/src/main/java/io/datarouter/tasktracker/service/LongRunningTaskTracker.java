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
import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.p;
import static j2html.TagCreator.text;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.email.email.DatarouterHtmlEmailService;
import io.datarouter.email.type.DatarouterEmailTypes.LongRunningTaskTrackerEmailType;
import io.datarouter.instrumentation.metric.MetricAnnotationLevel;
import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.instrumentation.task.TaskStatus;
import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
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
import io.datarouter.util.mutable.MutableBoolean;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;
import j2html.tags.specialized.BodyTag;

public class LongRunningTaskTracker implements TaskTracker{
	private static final Logger logger = LoggerFactory.getLogger(LongRunningTaskTracker.class);

	private static final Duration PERSIST_PERIOD = Duration.ofSeconds(1);
	private static final Duration CALLBACK_PERIOD = Duration.ofSeconds(5);
	private static final Duration ANNOTATION_DELAY = Duration.ofSeconds(60);

	private final DatarouterTaskTrackerPaths datarouterTaskTrackerPaths;
	private final DatarouterHtmlEmailService datarouterHtmlEmailService;
	private final ServerName serverName;
	private final LongRunningTaskGraphLink longRunningTaskGraphLink;
	private final Setting<Boolean> persistSetting;
	private final SortedMapStorage<LongRunningTaskKey,LongRunningTask> node;
	private final TaskTrackerCounters counters;
	private final ServerTypeDetector serverTypeDetector;
	private final LongRunningTaskTrackerEmailType longRunningTaskTrackerEmailType;
	private final Setting<Boolean> sendAlertEmail;
	private final StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;
	private final List<Consumer<LongRunningTaskTracker>> callbacks;
	private final TaskTrackerAlertReportService alertReportService;
	private final ServiceName serviceName;
	private final String environmentName;

	private final LongRunningTaskInfo task;
	private final Instant deadline;
	private final boolean warnOnReachingInterrupt;
	private final MutableBoolean stopRequested;
	private volatile Instant lastPersisted;
	private volatile Instant lastReportedCallback;
	private volatile boolean deadlineAlertAttempted;

	private Instant triggerTime;
	private boolean hasRecordedStartAnnotation;

	public LongRunningTaskTracker(
			DatarouterTaskTrackerPaths datarouterTaskTrackerPaths,
			DatarouterHtmlEmailService datarouterHtmlEmailService,
			ServerName serverName,
			LongRunningTaskGraphLink longRunningTaskGraphLink,
			Setting<Boolean> persistSetting,
			SortedMapStorage<LongRunningTaskKey,LongRunningTask> node,
			TaskTrackerCounters counters,
			ServerTypeDetector serverTypeDetector,
			LongRunningTaskTrackerEmailType longRunningTaskTrackerEmailType,
			Setting<Boolean> sendAlertEmail,
			StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService,
			LongRunningTaskInfo task,
			Instant deadline,
			boolean warnOnReachingInterrupt,
			TaskTrackerAlertReportService alertReportService,
			ServiceName serviceName,
			String environmentName){
		this.datarouterTaskTrackerPaths = datarouterTaskTrackerPaths;
		this.datarouterHtmlEmailService = datarouterHtmlEmailService;
		this.serverName = serverName;
		this.longRunningTaskGraphLink = longRunningTaskGraphLink;
		this.persistSetting = persistSetting;
		this.node = node;
		this.counters = counters;
		this.serverTypeDetector = serverTypeDetector;
		this.longRunningTaskTrackerEmailType = longRunningTaskTrackerEmailType;
		this.sendAlertEmail = sendAlertEmail;
		this.standardDatarouterEmailHeaderService = standardDatarouterEmailHeaderService;
		this.alertReportService = alertReportService;
		this.serviceName = serviceName;
		this.environmentName = environmentName;

		this.task = task;
		this.deadline = deadline;
		this.warnOnReachingInterrupt = warnOnReachingInterrupt;
		this.stopRequested = new MutableBoolean(false);
		this.deadlineAlertAttempted = false;
		this.hasRecordedStartAnnotation = false;

		this.callbacks = new ArrayList<>();
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
	public LongRunningTaskTracker setScheduledTime(Instant scheduledTime){
		task.triggerTimeMs = scheduledTime.toEpochMilli();
		return this;
	}

	@Override
	public Instant getScheduledTime(){
		return Optional.ofNullable(task.triggerTimeMs)
				.map(Instant::ofEpochMilli)
				.orElse(null);
	}

	public LongRunningTaskTracker setTriggerTime(Instant triggerTime){
		this.triggerTime = triggerTime;
		return this;
	}

	public Instant getTriggerTime(){
		return triggerTime;
	}

	@Override
	public LongRunningTaskTracker start(){
		Instant now = Instant.now();
		task.startTimeMs = now.toEpochMilli();
		if(getScheduledTime() == null){
			setScheduledTime(now);
		}
		setStatus(TaskStatus.RUNNING);
		doReportTasks();
		return this;
	}

	@Override
	public Instant getStartTime(){
		return Instant.ofEpochMilli(task.startTimeMs);
	}

	@Override
	public LongRunningTaskTracker finish(){
		TaskStatus finishStatus = getStatus() == TaskStatus.RUNNING ? TaskStatus.SUCCESS : getStatus();
		return onFinish(finishStatus);
	}

	public LongRunningTaskTracker onFinish(TaskStatus status){
		recordStartAnnotation();
		recordEndAnnotation();
		task.finishTimeMs = System.currentTimeMillis();
		setStatus(status);
		doReportTasks();
		return this;
	}

	@Override
	public Instant getFinishTime(){
		return Instant.ofEpochMilli(task.finishTimeMs);
	}

	/*------------ counting ---------------*/

	@Override
	public long getCount(){
		return task.numItemsProcessed.get();
	}

	@Override
	public String getLastItem(){
		return task.lastItemProcessed;
	}

	/*--------------- heartbeat --------------*/

	@Override
	public TaskTracker setCount(long value){
		task.numItemsProcessed.set(value);
		return this;
	}

	@Override
	public LongRunningTaskTracker increment(){
		return increment(1);
	}

	@Override
	public LongRunningTaskTracker increment(long delta){
		counters.increment(task.name, delta);
		task.numItemsProcessed.addAndGet(delta);
		return heartbeat();
	}

	@Override
	public LongRunningTaskTracker heartbeat(long numItemsProcessed){
		task.numItemsProcessed.set(numItemsProcessed);
		return heartbeat();
	}

	@Override
	public LongRunningTaskTracker heartbeat(){
		counters.heartbeat(task.name);
		task.heartbeatTimeMs = System.currentTimeMillis();
		recordStartAnnotation();
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
	public LongRunningTaskTracker setStatus(TaskStatus status){
		task.longRunningTaskStatus = LongRunningTaskStatus.BY_TASK_STATUS.fromOrNull(status);
		return this;
	}

	@Override
	public TaskStatus getStatus(){
		return task.longRunningTaskStatus.status;
	}

	/*------------ interrupt -----------------*/

	@Override
	public LongRunningTaskTracker requestStop(){
		logger.warn("requestStop on {}", task.name);
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
		if(deadline != null && Instant.now().isAfter(deadline)){
			task.longRunningTaskStatus = LongRunningTaskStatus.MAX_DURATION_REACHED;
			if(sendAlertEmail.get()){
				sendMaxDurationAlertIfShould();
			}
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

	public void addCallback(Consumer<LongRunningTaskTracker> callback){
		callbacks.add(callback);
	}

	private void recordStartAnnotation(){
		if(!hasRecordedStartAnnotation
				&& Duration.ofMillis(System.currentTimeMillis() - task.startTimeMs).compareTo(ANNOTATION_DELAY) > 0){
			hasRecordedStartAnnotation = true;
			Metrics.annotate("Datarouter job " + task.name + " start", "job", "", MetricAnnotationLevel.INFO,
					task.startTimeMs);
		}
	}

	private void recordEndAnnotation(){
		if(Duration.ofMillis(System.currentTimeMillis() - task.startTimeMs).compareTo(ANNOTATION_DELAY) > 0){
			Metrics.annotate("Datarouter job " + task.name + " end", "job", "");
		}
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
		String primaryHref = datarouterHtmlEmailService.startLinkBuilder()
				.withLocalPath(datarouterTaskTrackerPaths.datarouter.longRunningTasks)
				.withParam(LongRunningTasksHandler.P_name, task.name)
				.withParam(LongRunningTasksHandler.P_status, LongRunningTasksHandler.ALL_STATUSES_VALUE)
				.build();
		String counterHref = longRunningTaskGraphLink.getLink(task.name);
		var emailBuilder = datarouterHtmlEmailService.startEmailBuilder()
				.withTitle("Task Timeout")
				.withTitleHref(primaryHref)
				.withContent(makeEmailBody(task.name, serverName.get(), primaryHref, counterHref))
				.fromAdmin()
				.toAdmin(serverTypeDetector.mightBeDevelopment())
				.toSubscribers(serverTypeDetector.mightBeProduction())
				.to(longRunningTaskTrackerEmailType.tos, serverTypeDetector.mightBeProduction());
		alertReportService.reportTaskTimeoutAlert(
				serviceName.get(),
				serverName.get(),
				task.name,
				emailBuilder.getSubject(),
				emailBuilder,
				environmentName,
				counterHref,
				primaryHref);
	}

	private BodyTag makeEmailBody(String name, String serverName, String detailsHref, String counterHref){
		var header = standardDatarouterEmailHeaderService.makeStandardHeader();
		var message = p(
				a("Deadline reached for " + name).withHref(detailsHref),
				text(String.format(" on %s. Consider extending the trigger period.", serverName)));
		var tasksLink = a("Tasks").withHref(detailsHref);
		var counterLink = a("Counters").withHref(counterHref);
		return body(header, message, br(), tasksLink, br(), counterLink);
	}

	/*------------ persist -----------------*/

	private void reportIfEnoughTimeElapsed(){
		Instant now = Instant.now();
		if(lastPersisted == null || Duration.between(lastPersisted, now).compareTo(PERSIST_PERIOD) > 0){
			persist();
		}
		if(lastReportedCallback == null || Duration.between(lastReportedCallback, now).compareTo(CALLBACK_PERIOD) > 0){
			reportCallbacks();
		}
	}

	public void doReportTasks(){
		if(task.triggerTimeMs == null){
			logger.warn("setting null triggerTime to now on {}", task.databeanName);
			task.triggerTimeMs = System.currentTimeMillis();
		}
		reportCallbacks();
		persist();
	}

	private void reportCallbacks(){
		callbacks.forEach(callback -> {
			try{
				callback.accept(this);
			}catch(Exception e){
				logger.warn("Error reporting taskTracker to callback for {}", task.name, e);
			}
		});
		lastReportedCallback = Instant.now();
	}

	/*------------ exception -----------------*/

	public String getExceptionRecordId(){
		return task.exceptionRecordId;
	}

	public void setExceptionRecordId(String exceptionRecordId){
		task.exceptionRecordId = exceptionRecordId;
		doReportTasks();
	}

	private void persist(){
		if(node != null && persistSetting != null && persistSetting.get()){
			try{
				node.put(new LongRunningTask(task));
			}catch(RuntimeException e){
				logger.error("Failed to persist task={}", task, e);
			}
		}
		lastPersisted = Instant.now();
	}

	/*------------ subclass ------------*/

	public static Optional<LongRunningTaskTracker> findFromTaskTracker(TaskTracker tracker){
		return tracker instanceof LongRunningTaskTracker
				? Optional.of((LongRunningTaskTracker)tracker)
				: Optional.empty();
	}

}
