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
package io.datarouter.tasktracker.config;

import java.util.List;

import io.datarouter.instrumentation.task.TaskTrackerPublisher;
import io.datarouter.instrumentation.task.TaskTrackerPublisher.NoOpTaskTrackerPublisher;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.tasktracker.service.DefaultTaskTrackerAlertReportService;
import io.datarouter.tasktracker.service.LongRunningTaskDailyDigest;
import io.datarouter.tasktracker.service.TaskTrackerAlertReportService;
import io.datarouter.tasktracker.storage.LongRunningTaskDao;
import io.datarouter.tasktracker.storage.LongRunningTaskDao.LongRunningTaskDaoParams;
import io.datarouter.tasktracker.web.LongRunningTaskGraphLink;
import io.datarouter.tasktracker.web.LongRunningTaskGraphLink.NoOpLongRunningTaskGraphLink;
import io.datarouter.tasktracker.web.TaskTrackerExceptionLink;
import io.datarouter.tasktracker.web.TaskTrackerExceptionLink.NoOpTaskTrackerExceptionLink;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterTaskTrackerPlugin extends BaseWebPlugin{

	private static final DatarouterTaskTrackerPaths PATHS = new DatarouterTaskTrackerPaths();

	private final Class<? extends LongRunningTaskGraphLink> longRunningTaskGraphLink;
	private final DatarouterTaskTrackerDaoModule daosModuleBuilder;
	private final Class<? extends TaskTrackerExceptionLink> exceptionLink;
	private final Class<? extends TaskTrackerPublisher> longRunningTaskPublisher;
	private final Class<? extends TaskTrackerAlertReportService> taskTrackerAlertReportService;

	private DatarouterTaskTrackerPlugin(
			Class<? extends LongRunningTaskGraphLink> longRunningTaskGraphLink,
			Class<? extends TaskTrackerExceptionLink> exceptionLink,
			Class<? extends TaskTrackerPublisher> longRunningTaskPublisher,
			Class<? extends TaskTrackerAlertReportService> taskTrackerAlertReportService,
			DatarouterTaskTrackerDaoModule daosModuleBuilder){
		this.longRunningTaskGraphLink = longRunningTaskGraphLink;
		this.exceptionLink = exceptionLink;
		this.longRunningTaskPublisher = longRunningTaskPublisher;
		this.daosModuleBuilder = daosModuleBuilder;
		this.taskTrackerAlertReportService = taskTrackerAlertReportService;

		addDatarouterNavBarItem(DatarouterNavBarCategory.JOBS, PATHS.datarouter.longRunningTasks, "Long-Running Tasks");
		addDatarouterNavBarItem(DatarouterNavBarCategory.JOBS, PATHS.datarouter.jobsHealth, "Jobs Health");
		addSettingRoot(DatarouterTaskTrackerSettingRoot.class);
		addRouteSet(DatarouterTaskTrackerRouteSet.class);
		addDatarouterGithubDocLink("datarouter-task-tracker");
		addDailyDigest(LongRunningTaskDailyDigest.class);
	}

	@Override
	public DatarouterTaskTrackerDaoModule getDaosModuleBuilder(){
		return daosModuleBuilder;
	}

	@Override
	public void configure(){
		if(longRunningTaskGraphLink != null){
			bindActual(LongRunningTaskGraphLink.class, longRunningTaskGraphLink);
		}
		bind(TaskTrackerExceptionLink.class).to(exceptionLink);
		bind(TaskTrackerPublisher.class).to(longRunningTaskPublisher);
		bind(TaskTrackerAlertReportService.class).to(taskTrackerAlertReportService);
	}

	public static class DatarouterTaskTrackerDaoModule extends DaosModuleBuilder{

		private final List<ClientId> longRunningTaskClientId;

		public DatarouterTaskTrackerDaoModule(List<ClientId> longRunningTaskClientId){
			this.longRunningTaskClientId = longRunningTaskClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(LongRunningTaskDao.class);
		}

		@Override
		public void configure(){
			bind(LongRunningTaskDaoParams.class).toInstance(new LongRunningTaskDaoParams(longRunningTaskClientId));
		}

	}

	public static class DatarouterTaskTrackerPluginBuilder{

		private final List<ClientId> defaultClientId;

		private Class<? extends LongRunningTaskGraphLink> longRunningTaskGraphLink = NoOpLongRunningTaskGraphLink.class;
		private Class<? extends TaskTrackerExceptionLink> exceptionLink = NoOpTaskTrackerExceptionLink.class;
		private Class<? extends TaskTrackerPublisher> longRunningTaskPublisher = NoOpTaskTrackerPublisher.class;
		private Class<? extends TaskTrackerAlertReportService> taskTrackerAlertReportService =
				DefaultTaskTrackerAlertReportService.class;

		public DatarouterTaskTrackerPluginBuilder(List<ClientId> defaultClientId){
			this.defaultClientId = defaultClientId;
		}

		public DatarouterTaskTrackerPluginBuilder(ClientId defaultClientId){
			this(List.of(defaultClientId));
		}

		public DatarouterTaskTrackerPluginBuilder setLongRunningTaskGraphLinkClass(
				Class<? extends LongRunningTaskGraphLink> longRunningTaskGraphLink){
			this.longRunningTaskGraphLink = longRunningTaskGraphLink;
			return this;
		}

		public DatarouterTaskTrackerPluginBuilder setExceptionLinkClass(
				Class<? extends TaskTrackerExceptionLink> exceptionLink){
			this.exceptionLink = exceptionLink;
			return this;
		}

		public DatarouterTaskTrackerPluginBuilder setLongRunningTaskPublisher(
				Class<? extends TaskTrackerPublisher> longRunningTaskPublisher){
			this.longRunningTaskPublisher = longRunningTaskPublisher;
			return this;
		}

		public DatarouterTaskTrackerPluginBuilder setTaskTrackerAlertReportService(
				Class<? extends TaskTrackerAlertReportService> taskTrackerAlertReportService){
			this.taskTrackerAlertReportService = taskTrackerAlertReportService;
			return this;
		}

		public DatarouterTaskTrackerPlugin build(){
			return new DatarouterTaskTrackerPlugin(
					longRunningTaskGraphLink,
					exceptionLink,
					longRunningTaskPublisher,
					taskTrackerAlertReportService,
					new DatarouterTaskTrackerDaoModule(defaultClientId));
		}

	}

}
