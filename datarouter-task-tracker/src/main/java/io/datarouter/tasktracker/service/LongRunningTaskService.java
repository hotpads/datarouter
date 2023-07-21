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

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.tasktracker.scheduler.LongRunningTaskStatus;
import io.datarouter.tasktracker.storage.LongRunningTask;
import io.datarouter.tasktracker.storage.LongRunningTaskDao;
import io.datarouter.tasktracker.storage.LongRunningTaskKey;
import io.datarouter.util.tuple.Range;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class LongRunningTaskService{
	private static final Logger logger = LoggerFactory.getLogger(LongRunningTaskService.class);

	@Inject
	private LongRunningTaskDao dao;

	public Optional<LongRunningTask> getLastRun(TaskTracker tracker){
		String name = tracker.getName();
		LongRunningTaskKey firstPrefix = new LongRunningTaskKey(name, null, null);
		LongRunningTaskKey lastPrefix = new LongRunningTaskKey(name, Date.from(tracker.getScheduledTime()),
				tracker.getServerName());
		Range<LongRunningTaskKey> range = new Range<>(firstPrefix, true, lastPrefix, false);
		return dao.scan(range)
				.reduce((firstRun, secondRun) -> secondRun);
	}

	@Deprecated // use instants
	public Optional<Date> findLastSuccessDate(String name){
		LongRunningTaskKey key = new LongRunningTaskKey(name, null, null);
		return dao.scanWithPrefix(key)
				.include(task -> task.getJobExecutionStatus() == LongRunningTaskStatus.SUCCESS)
				.map(LongRunningTask::getFinishTime)
				.findMax(Date::compareTo);
	}

	public Optional<Instant> findLastSuccessInstant(String name){
		LongRunningTaskKey key = new LongRunningTaskKey(name, null, null);
		return dao.scanWithPrefix(key)
				.include(task -> task.getJobExecutionStatus() == LongRunningTaskStatus.SUCCESS)
				.map(LongRunningTask::getFinishTimeInstant)
				.exclude(Objects::isNull)
				.findMax(Instant::compareTo);
	}

	public boolean isRunning(String name){
		LongRunningTaskKey key = new LongRunningTaskKey(name, null, null);
		return dao.scanWithPrefix(key)
				.findLast()
				.map(LongRunningTask::isRunning)
				.orElse(false);
	}

	public Optional<LongRunningTask> findLastNonRunningStatusTask(String name){
		LongRunningTaskKey key = new LongRunningTaskKey(name, null, null);
		return dao.scanWithPrefix(key)
				.exclude(task -> task.getJobExecutionStatus() == LongRunningTaskStatus.RUNNING)
				.exclude(task -> {
					if(task.getFinishTimeInstant() == null){
						logger.warn("LongRunningTask={} with status={} has null finishedTime.", task, task
								.getJobExecutionStatus());
						return true;
					}
					return false;
				})
				.sort(Comparator.comparing(LongRunningTask::getFinishTimeInstant))
				.findLast();
	}

	public LongRunningTaskSummaryDto getSummary(){
		Map<String,LongRunningTask> lastCompletions = new HashMap<>();
		Map<String,LongRunningTask> currentlyRunningTasks = new HashMap<>();
		Map<String,SortedSet<String>> runningOnServers = new HashMap<>();
		for(LongRunningTask task : dao.scan().iterable()){
			String name = task.getKey().getName();
			if(task.isRunning()){
				// currentlyRunningTasks
				LongRunningTask current = currentlyRunningTasks.get(name);
				if(current == null || task.getStartTime().after(current.getStartTime())){
					currentlyRunningTasks.put(name, task);
				}
				// runningOnServers
				runningOnServers.putIfAbsent(name, new TreeSet<>());
				runningOnServers.get(name).add(task.getKey().getServerName());
			}
			// lastCompletions
			if(task.isSuccess()){
				LongRunningTask current = lastCompletions.get(name);
				if(current == null || task.getFinishTimeInstant().isAfter(current.getFinishTimeInstant())){
					lastCompletions.put(name, task);
				}
			}
		}
		return new LongRunningTaskSummaryDto(lastCompletions, currentlyRunningTasks, runningOnServers);
	}


	public static class LongRunningTaskSummaryDto{

		public final Map<String,LongRunningTask> lastCompletions;
		public final Map<String,LongRunningTask> currentlyRunningTasks;
		public final Map<String,SortedSet<String>> runningOnServers;

		public LongRunningTaskSummaryDto(Map<String,LongRunningTask> lastCompletions,
				Map<String,LongRunningTask> currentlyRunningTasks, Map<String,SortedSet<String>> runningOnServers){
			this.lastCompletions = lastCompletions;
			this.currentlyRunningTasks = currentlyRunningTasks;
			this.runningOnServers = runningOnServers;
		}
	}

}
