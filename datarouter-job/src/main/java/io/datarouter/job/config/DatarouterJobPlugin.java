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
package io.datarouter.job.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.job.TriggerGroupClasses;
import io.datarouter.job.detached.DefaultDetachedJobExecutor;
import io.datarouter.job.detached.DetachedJobExecutor;
import io.datarouter.job.metriclink.AppJobsMetricLinkPage;
import io.datarouter.job.metriclink.DatarouterJobsMetricLinkPage;
import io.datarouter.job.scheduler.JobSchedulerAppListener;
import io.datarouter.job.storage.clusterjoblock.DatarouterClusterJobLockDao;
import io.datarouter.job.storage.clusterjoblock.DatarouterClusterJobLockDao.DatarouterClusterJobLockDaoParams;
import io.datarouter.job.storage.clustertriggerlock.DatarouterClusterTriggerLockDao;
import io.datarouter.job.storage.clustertriggerlock.DatarouterClusterTriggerLockDao.DatarouterClusterTriggerLockDaoParams;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.dispatcher.DatarouterWebRouteSet;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterJobPlugin extends BaseJobPlugin{

	private final List<Class<? extends BaseTriggerGroup>> triggerGroupClasses;
	private final Class<? extends DetachedJobExecutor> detachedJobExecutorClass;

	private DatarouterJobPlugin(
			DatarouterJobDaoModule daosModuleBuilder,
			Class<? extends DetachedJobExecutor> detachedJobExecutorClass){
		this(null, daosModuleBuilder, detachedJobExecutorClass);
	}

	private DatarouterJobPlugin(
			List<Class<? extends BaseTriggerGroup>> triggerGroupClasses,
			DatarouterJobDaoModule daosModuleBuilder,
			Class<? extends DetachedJobExecutor> detachedJobExecutorClass){
		this.triggerGroupClasses = triggerGroupClasses;
		this.detachedJobExecutorClass = detachedJobExecutorClass;

		addAppListener(JobSchedulerAppListener.class);
		addRouteSetOrdered(DatarouterJobRouteSet.class, DatarouterWebRouteSet.class);
		addSettingRoot(DatarouterJobSettingRoot.class);
		addTriggerGroup(DatarouterJobTriggerGroup.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterNavBarItem(DatarouterNavBarCategory.JOBS, new DatarouterJobPaths().datarouter.triggers.list,
				"Triggers");
		addTestable(DatarouterJobBootstrapIntegrationService.class);
		addDatarouterGithubDocLink("datarouter-job");
		addMetricLinkPages(AppJobsMetricLinkPage.class);
		addMetricLinkPages(DatarouterJobsMetricLinkPage.class);
	}

	@Override
	public String getName(){
		return "DatarouterJob";
	}

	@Override
	public void configure(){
		bind(TriggerGroupClasses.class).toInstance(new TriggerGroupClasses(triggerGroupClasses));
		bind(DetachedJobExecutor.class).to(this.detachedJobExecutorClass);
	}

	public static class DatarouterJobDaoModule extends DaosModuleBuilder{

		private final List<ClientId> datarouterClusterJobLockClientIds;
		private final List<ClientId> datarouterClusterTriggerLockClientIds;

		public DatarouterJobDaoModule(
				List<ClientId> datarouterClusterJobLockClientIds,
				List<ClientId> datarouterClusterTriggerLockClientIds){
			this.datarouterClusterJobLockClientIds = datarouterClusterJobLockClientIds;
			this.datarouterClusterTriggerLockClientIds = datarouterClusterTriggerLockClientIds;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(
					DatarouterClusterJobLockDao.class,
					DatarouterClusterTriggerLockDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterClusterTriggerLockDaoParams.class)
					.toInstance(new DatarouterClusterTriggerLockDaoParams(datarouterClusterTriggerLockClientIds));
			bind(DatarouterClusterJobLockDaoParams.class)
					.toInstance(new DatarouterClusterJobLockDaoParams(datarouterClusterJobLockClientIds));
		}

	}

	public static class DatarouterJobPluginBuilder{

		private final List<ClientId> defaultClientIds;

		private List<Class<? extends BaseTriggerGroup>> triggerGroupClasses = new ArrayList<>();

		private Class<? extends DetachedJobExecutor> detachedJobExecutorClass = DefaultDetachedJobExecutor.class;

		public DatarouterJobPluginBuilder(List<ClientId> defaultClientIds){
			this.defaultClientIds = defaultClientIds;
		}

		public DatarouterJobPluginBuilder setTriggerGroupClasses(
				List<Class<? extends BaseTriggerGroup>> triggerGroupClasses){
			this.triggerGroupClasses.addAll(triggerGroupClasses);
			return this;
		}

		public DatarouterJobPluginBuilder withDetachedJobExecutorClass(
				Class<? extends DetachedJobExecutor> detachedJobExecutorClass){
			this.detachedJobExecutorClass = detachedJobExecutorClass;
			return this;
		}

		public DatarouterJobPlugin getSimplePluginData(){
			return new DatarouterJobPlugin(
					new DatarouterJobDaoModule(defaultClientIds, defaultClientIds),
					detachedJobExecutorClass);
		}

		public DatarouterJobPlugin build(){
			return new DatarouterJobPlugin(
					triggerGroupClasses,
					new DatarouterJobDaoModule(defaultClientIds, defaultClientIds),
					detachedJobExecutorClass);
		}

	}

}
