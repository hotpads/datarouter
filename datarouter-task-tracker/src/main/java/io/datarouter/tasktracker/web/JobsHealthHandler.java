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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.tasktracker.config.DatarouterTaskTrackerFiles;
import io.datarouter.tasktracker.storage.LongRunningTask;
import io.datarouter.tasktracker.storage.LongRunningTaskDao;
import io.datarouter.tasktracker.storage.LongRunningTaskKey;
import io.datarouter.tasktracker.web.LongRunningTasksHandler.LongRunningTaskJspDto;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.user.session.CurrentUserSessionInfoService;

public class JobsHealthHandler extends BaseHandler{

	@Inject
	private LongRunningTaskDao longRunningTaskDao;
	@Inject
	private DatarouterTaskTrackerFiles files;
	@Inject
	private CurrentUserSessionInfoService currentUserSessionInfoService;

	@Handler(defaultHandler = true)
	Mav uniqueTasks(){
		Mav mav = new Mav(files.jsp.admin.datarouter.tasktracker.jobsHealthJsp);
		List<LongRunningTask> allTasks = longRunningTaskDao.scan()
				.include(task -> task.getStartTime() == null
						|| task.getStartTime().getTime() > System.currentTimeMillis() - Duration.ofDays(1).toMillis())
				.list();
		List<String> uniqueJobs = allTasks.stream()
				.map(LongRunningTask::getKey)
				.map(LongRunningTaskKey::getName)
				.distinct()
				.collect(Collectors.toList());
		Integer numRunningJobs = 0;
		ZoneId zoneId = currentUserSessionInfoService.getZoneId(request);
		List<LongRunningTaskJspDto> allBadTasks = new ArrayList<>();
		for(LongRunningTask task : allTasks){
			if(task.isBadState()){
				allBadTasks.add(new LongRunningTaskJspDto(task, zoneId));
			}else if(task.isRunning()){
				numRunningJobs++;
			}
		}
		mav.put("allBadTasks", allBadTasks);
		mav.put("uniqueJobs", uniqueJobs);
		mav.put("numUniqueJobs", uniqueJobs.size());
		mav.put("numRunningJobs", numRunningJobs);
		mav.put("legend", LongRunningTasksHandler.legend());
		return mav;
	}

}
