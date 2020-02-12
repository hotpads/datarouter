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

	private DatarouterJobPlugin(DatarouterJobDaoModule daosModuleBuilder){
		this(null, daosModuleBuilder);
	}

	private DatarouterJobPlugin(
			List<Class<? extends BaseTriggerGroup>> triggerGroupClasses,
			DatarouterJobDaoModule daosModuleBuilder){
		addAppListener(JobSchedulerAppListener.class);
		addRouteSetOrdered(DatarouterJobRouteSet.class, DatarouterWebRouteSet.class);
		addSettingRoot(DatarouterJobSettingRoot.class);
		addTriggerGroup(DatarouterJobTriggerGroup.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterNavBarItem(DatarouterNavBarCategory.JOBS, new DatarouterJobPaths().datarouter.triggers.list,
				"Triggers");
		this.triggerGroupClasses = triggerGroupClasses;
	}

	@Override
	public String getName(){
		return "DatarouterJob";
	}

	@Override
	public void configure(){
		bind(TriggerGroupClasses.class).toInstance(new TriggerGroupClasses(triggerGroupClasses));
	}

	public static class DatarouterJobDaoModule extends DaosModuleBuilder{

		private final ClientId datarouterClusterJobLockClientId;
		private final ClientId datarouterClusterTriggerLockClientId;

		public DatarouterJobDaoModule(
				ClientId datarouterClusterJobLockClientId,
				ClientId datarouterClusterTriggerLockClientId){
			this.datarouterClusterJobLockClientId = datarouterClusterJobLockClientId;
			this.datarouterClusterTriggerLockClientId = datarouterClusterTriggerLockClientId;
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
					.toInstance(new DatarouterClusterTriggerLockDaoParams(datarouterClusterTriggerLockClientId));
			bind(DatarouterClusterJobLockDaoParams.class)
					.toInstance(new DatarouterClusterJobLockDaoParams(datarouterClusterJobLockClientId));
		}

	}

	public static class DatarouterJobPluginBuilder{

		private final ClientId defaultClientId;
		private DatarouterJobDaoModule daoModule;

		private List<Class<? extends BaseTriggerGroup>> triggerGroupClasses = new ArrayList<>();

		public DatarouterJobPluginBuilder(ClientId defaultClientId){
			this.defaultClientId = defaultClientId;
		}

		public DatarouterJobPluginBuilder setTriggerGroupClasses(
				List<Class<? extends BaseTriggerGroup>> triggerGroupClasses){
			this.triggerGroupClasses.addAll(triggerGroupClasses);
			return this;
		}

		public DatarouterJobPluginBuilder setDaoModule(DatarouterJobDaoModule daoModule){
			this.daoModule = daoModule;
			return this;
		}

		public DatarouterJobPlugin getSimplePluginData(){
			return new DatarouterJobPlugin(
					daoModule == null ? new DatarouterJobDaoModule(defaultClientId, defaultClientId) : daoModule);
		}

		public DatarouterJobPlugin build(){
			return new DatarouterJobPlugin(
					triggerGroupClasses,
					daoModule == null ? new DatarouterJobDaoModule(defaultClientId, defaultClientId) : daoModule);
		}

	}

}
