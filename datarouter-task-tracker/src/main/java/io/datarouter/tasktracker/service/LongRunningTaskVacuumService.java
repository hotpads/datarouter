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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.instrumentation.task.TaskTracker;
import io.datarouter.model.databean.Databean;
import io.datarouter.scanner.Scanner;
import io.datarouter.tasktracker.config.DatarouterTaskTrackerSettingRoot;
import io.datarouter.tasktracker.storage.LongRunningTask;
import io.datarouter.tasktracker.storage.LongRunningTaskDao;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.lang.ObjectTool;

@Singleton
public class LongRunningTaskVacuumService{

	@Inject
	private LongRunningTaskDao dao;
	@Inject
	private DatarouterTaskTrackerSettingRoot settings;

	public void run(TaskTracker tracker){
		List<LongRunningTask> relatedTasks = new ArrayList<>();
		for(LongRunningTask task : dao.scan().iterable()){
			String name = task.getKey().getName();
			if(!relatedTasks.isEmpty()){
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
		Instant tooOldCutoff = Instant.now().minus(settings.maxAge.get().toJavaDuration());
		List<LongRunningTask> tooOld = tasks.stream()
				.filter(task -> task.getKey().getTriggerTime().toInstant().isBefore(tooOldCutoff))
				.collect(Collectors.toList());
		Scanner.of(tooOld)
				.map(Databean::getKey)
				.then(dao::deleteBatched);

		// keep the latest N
		List<LongRunningTask> remaining = new ArrayList<>(tasks);
		remaining.removeAll(tooOld);
		if(remaining.size() <= settings.countToKeep.get()){
			return;
		}
		Scanner.of(remaining)
				.limit(remaining.size() - settings.countToKeep.get())
				.map(Databean::getKey)
				.then(dao::deleteBatched);
	}

}
