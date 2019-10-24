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

import java.time.Instant;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.storage.config.DatarouterAdministratorEmailService;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.tasktracker.TaskTrackerCounters;
import io.datarouter.tasktracker.config.DatarouterTaskTrackerPaths;
import io.datarouter.tasktracker.config.DatarouterTaskTrackerSettingRoot;
import io.datarouter.tasktracker.scheduler.LongRunningTaskType;
import io.datarouter.tasktracker.storage.DatarouterLongRunningTaskDao;
import io.datarouter.tasktracker.web.LongRunningTaskGraphLink;
import io.datarouter.web.email.DatarouterEmailService;

@Singleton
public class LongRunningTaskTrackerFactory{

	@Inject
	private DatarouterEmailService datarouterEmailService;
	@Inject
	private DatarouterProperties datarouterProperties;
	@Inject
	private DatarouterService datarouterService;
	@Inject
	private DatarouterAdministratorEmailService datarouterAdministratorEmailService;
	@Inject
	private LongRunningTaskGraphLink longRunningTaskGraphLink;
	@Inject
	private DatarouterTaskTrackerSettingRoot settings;
	@Inject
	private DatarouterLongRunningTaskDao longRunningTaskDao;
	@Inject
	private TaskTrackerCounters counters;
	@Inject
	private DatarouterTaskTrackerPaths paths;

	public LongRunningTaskTracker create(
			Class<?> trackedClass,
			LongRunningTaskType type,
			Instant deadline,
			boolean warnOnReachingDeadline,
			String triggeredBy){
		LongRunningTaskInfo task = new LongRunningTaskInfo(
				trackedClass,
				datarouterProperties.getServerName(),
				type,
				triggeredBy);
		return new LongRunningTaskTracker(
				datarouterEmailService,
				datarouterProperties,
				datarouterService,
				datarouterAdministratorEmailService,
				longRunningTaskGraphLink,
				settings.saveLongRunningTasks,
				longRunningTaskDao.getNode(),
				counters,
				task,
				deadline,
				warnOnReachingDeadline,
				paths.datarouter.longRunningTasks.toSlashedString());
	}

	public static String taskNameForClass(Class<?> cls){
		return cls.getSimpleName();
	}

}
