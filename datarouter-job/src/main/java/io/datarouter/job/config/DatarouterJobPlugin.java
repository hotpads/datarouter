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
package io.datarouter.job.config;

import java.util.List;

import io.datarouter.instrumentation.metric.MetricLinkBuilder;
import io.datarouter.instrumentation.metric.MetricLinkBuilder.NoOpMetricLinkBuilder;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.detached.DefaultDetachedJobExecutor;
import io.datarouter.job.detached.DefaultDetachedJobStopper;
import io.datarouter.job.detached.DetachedJobExecutor;
import io.datarouter.job.detached.DetachedJobStopper;
import io.datarouter.job.metriclink.AppJobsMetricLinkPage;
import io.datarouter.job.metriclink.DatarouterJobsMetricLinkPage;
import io.datarouter.job.scheduler.JobSchedulerAppListener;
import io.datarouter.job.storage.joblock.DatarouterJobLockDao;
import io.datarouter.job.storage.joblock.DatarouterJobLockDao.DatarouterJobLockDaoParams;
import io.datarouter.job.storage.stopjobrequest.StopJobRequestDao;
import io.datarouter.job.storage.stopjobrequest.StopJobRequestDao.StopJobRequestDaoParams;
import io.datarouter.job.storage.triggerlock.DatarouterTriggerLockDao;
import io.datarouter.job.storage.triggerlock.DatarouterTriggerLockDao.DatarouterTriggerLockDaoParams;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.dispatcher.DatarouterWebRouteSet;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterJobPlugin extends BaseWebPlugin{

	private DatarouterJobPlugin(
			DatarouterJobDaoModule daosModuleBuilder,
			Class<? extends DetachedJobExecutor> detachedJobExecutor,
			Class<? extends DetachedJobStopper> detachedJobStopper){

		addAppListener(JobSchedulerAppListener.class);
		addRouteSetOrdered(DatarouterJobRouteSet.class, DatarouterWebRouteSet.class);
		addSettingRoot(DatarouterJobSettingRoot.class);
		addPluginEntry(BaseTriggerGroup.KEY, DatarouterJobTriggerGroup.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.JOBS,
				new DatarouterJobPaths().datarouter.triggers.list,
				"Triggers");
		addTestable(DatarouterJobBootstrapIntegrationService.class);
		addDatarouterGithubDocLink("datarouter-job");
		addMetricLinkPages(AppJobsMetricLinkPage.class);
		addMetricLinkPages(DatarouterJobsMetricLinkPage.class);
		addPluginEntry(DetachedJobExecutor.KEY, detachedJobExecutor);
		addPluginEntry(DetachedJobStopper.KEY, detachedJobStopper);
	}

	public static class DatarouterJobDaoModule extends DaosModuleBuilder{

		private final List<ClientId> datarouterClusterJobLockClientIds;
		private final List<ClientId> datarouterClusterTriggerLockClientIds;
		private final List<ClientId> stopJobRequestClientIds;
		private final Class<? extends MetricLinkBuilder> metricLinkBuilder;

		public DatarouterJobDaoModule(
				List<ClientId> datarouterClusterJobLockClientIds,
				List<ClientId> datarouterClusterTriggerLockClientIds,
				List<ClientId> stopJobRequestClientIds,
				Class<? extends MetricLinkBuilder> metricLinkBuilder){
			this.datarouterClusterJobLockClientIds = datarouterClusterJobLockClientIds;
			this.datarouterClusterTriggerLockClientIds = datarouterClusterTriggerLockClientIds;
			this.stopJobRequestClientIds = stopJobRequestClientIds;
			this.metricLinkBuilder = metricLinkBuilder;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(
					DatarouterJobLockDao.class,
					DatarouterTriggerLockDao.class,
					StopJobRequestDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterTriggerLockDaoParams.class)
					.toInstance(new DatarouterTriggerLockDaoParams(datarouterClusterTriggerLockClientIds));
			bind(DatarouterJobLockDaoParams.class)
					.toInstance(new DatarouterJobLockDaoParams(datarouterClusterJobLockClientIds));
			bind(StopJobRequestDaoParams.class)
					.toInstance(new StopJobRequestDaoParams(stopJobRequestClientIds));
			bindDefault(MetricLinkBuilder.class, metricLinkBuilder);
		}

	}

	public static class DatarouterJobPluginBuilder{

		private final List<ClientId> defaultClientIds;

		private Class<? extends DetachedJobExecutor> detachedJobExecutorClass = DefaultDetachedJobExecutor.class;
		private Class<? extends DetachedJobStopper> detachedJobStopperClass = DefaultDetachedJobStopper.class;
		private Class<? extends MetricLinkBuilder> metricLinkBuilder = NoOpMetricLinkBuilder.class;

		public DatarouterJobPluginBuilder(List<ClientId> defaultClientIds){
			this.defaultClientIds = defaultClientIds;
		}

		public DatarouterJobPluginBuilder withDetachedJobExecutorClass(
				Class<? extends DetachedJobExecutor> detachedJobExecutorClass){
			this.detachedJobExecutorClass = detachedJobExecutorClass;
			return this;
		}

		public DatarouterJobPluginBuilder withDetachedJobStopperClass(
				Class<? extends DetachedJobStopper> detachedJobStopperClass){
			this.detachedJobStopperClass = detachedJobStopperClass;
			return this;
		}

		public DatarouterJobPluginBuilder withMetricLinkBuilder(Class<? extends MetricLinkBuilder> metricLinkBuilder){
			this.metricLinkBuilder = metricLinkBuilder;
			return this;
		}

		public DatarouterJobPlugin getSimplePluginData(){
			return new DatarouterJobPlugin(
					new DatarouterJobDaoModule(defaultClientIds, defaultClientIds, defaultClientIds, metricLinkBuilder),
					detachedJobExecutorClass, detachedJobStopperClass);
		}

		public DatarouterJobPlugin build(){
			return new DatarouterJobPlugin(
					new DatarouterJobDaoModule(defaultClientIds, defaultClientIds, defaultClientIds, metricLinkBuilder),
					detachedJobExecutorClass, detachedJobStopperClass);
		}

	}

}
