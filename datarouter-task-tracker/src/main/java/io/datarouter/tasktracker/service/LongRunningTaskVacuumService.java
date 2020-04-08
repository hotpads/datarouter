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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.tasktracker.storage.DatarouterLongRunningTaskDao;
import io.datarouter.tasktracker.storage.LongRunningTask;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.lang.ObjectTool;

@Singleton
public class LongRunningTaskVacuumService{

	private static final Duration DELETE_OLDER_THAN = Duration.ofDays(100);
	private static final Integer KEEP_LATEST_N = 5;

	@Inject
	private DatarouterLongRunningTaskDao dao;

	public void run(TaskTracker tracker){
		List<LongRunningTask> relatedTasks = new ArrayList<>();
		for(LongRunningTask task : dao.scan().iterable()){
			String name = task.getKey().getName();
			if(CollectionTool.notEmpty(relatedTasks)){
				String previousName = ListTool.getLast(relatedTasks).getKey().getName();
				if(ObjectTool.notEquals(previousName, name)){
					vacuumRelatedTasks(relatedTasks);
					relatedTasks = new ArrayList<>();
				}
			}
			relatedTasks.add(task);
			if(tracker.increment().setLastItemProcessed(name).shouldStop()){
				return;
			}
		}
		vacuumRelatedTasks(relatedTasks);
	}

	private void vacuumRelatedTasks(List<LongRunningTask> tasks){
		// remove really old entries
		Instant tooOldCutoff = Instant.now().minus(DELETE_OLDER_THAN);
		List<LongRunningTask> tooOld = tasks.stream()
				.filter(task -> task.getKey().getTriggerTime().toInstant().isBefore(tooOldCutoff))
				.collect(Collectors.toList());
		dao.deleteMulti(DatabeanTool.getKeys(tooOld));
		// keep the latest N
		List<LongRunningTask> remaining = new ArrayList<>(tasks);
		remaining.removeAll(tooOld);
		if(remaining.size() <= KEEP_LATEST_N){
			return;
		}
		Scanner.of(remaining)
				.limit(remaining.size() - KEEP_LATEST_N)
				.map(Databean::getKey)
				.batch(100)
				.forEach(dao::deleteMulti);
	}

}
