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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.email.type.DatarouterEmailTypes.LongRunningTaskTrackerEmailType;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.tasktracker.TaskTrackerCounters;
import io.datarouter.tasktracker.config.DatarouterTaskTrackerPaths;
import io.datarouter.tasktracker.config.DatarouterTaskTrackerSettingRoot;
import io.datarouter.tasktracker.scheduler.LongRunningTaskType;
import io.datarouter.tasktracker.storage.LongRunningTaskDao;
import io.datarouter.tasktracker.web.LongRunningTaskGraphLink;
import io.datarouter.web.config.service.ServiceName;
import io.datarouter.web.email.DatarouterHtmlEmailService;
import io.datarouter.web.email.StandardDatarouterEmailHeaderService;

@Singleton
public class LongRunningTaskTrackerFactory{

	@Inject
	private DatarouterTaskTrackerPaths datarouterTaskTrackerPaths;
	@Inject
	private DatarouterHtmlEmailService datarouterHtmlEmailService;
	@Inject
	private ServerName serverName;
	@Inject
	private LongRunningTaskGraphLink longRunningTaskGraphLink;
	@Inject
	private DatarouterTaskTrackerSettingRoot settings;
	@Inject
	private LongRunningTaskDao longRunningTaskDao;
	@Inject
	private TaskTrackerCounters counters;
	@Inject
	private ServerTypeDetector serverTypeDetector;
	@Inject
	private LongRunningTaskTrackerEmailType longRunningTaskTrackerEmailType;
	@Inject
	private StandardDatarouterEmailHeaderService standardDatarouterEmailHeaderService;
	@Inject
	private TaskTrackerAlertReportService alertReportService;
	@Inject
	private ServiceName serviceName;
	@Inject
	private EnvironmentName environmentName;

	public LongRunningTaskTracker create(
			String name,
			LongRunningTaskType type,
			Instant deadline,
			boolean warnOnReachingDeadline,
			String triggeredBy){
		LongRunningTaskInfo task = new LongRunningTaskInfo(
				name,
				serverName.get(),
				type,
				triggeredBy);
		return new LongRunningTaskTracker(
				datarouterTaskTrackerPaths,
				datarouterHtmlEmailService,
				serverName,
				longRunningTaskGraphLink,
				settings.saveLongRunningTasks,
				longRunningTaskDao.getNode(),
				counters,
				serverTypeDetector,
				longRunningTaskTrackerEmailType,
				settings.sendAlertEmail,
				standardDatarouterEmailHeaderService,
				task,
				deadline,
				warnOnReachingDeadline,
				alertReportService,
				serviceName,
				environmentName.get());
	}

	public static String taskNameForClass(Class<?> cls){
		return cls.getSimpleName();
	}

}
