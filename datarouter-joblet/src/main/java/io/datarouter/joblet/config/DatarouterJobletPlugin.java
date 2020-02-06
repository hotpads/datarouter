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
package io.datarouter.joblet.config;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Provides;

import io.datarouter.joblet.DatarouterJobletAppListener;
import io.datarouter.joblet.nav.JobletExternalLinkBuilder;
import io.datarouter.joblet.nav.JobletExternalLinkBuilder.NoOpJobletExternalLinkBuilder;
import io.datarouter.joblet.setting.BaseJobletPlugin;
import io.datarouter.joblet.setting.DatarouterJobletSettingRoot;
import io.datarouter.joblet.storage.jobletdata.DatarouterJobletDataDao;
import io.datarouter.joblet.storage.jobletdata.DatarouterJobletDataDao.DatarouterJobletDataDaoParams;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao.DatarouterJobletRequestDaoParams;
import io.datarouter.joblet.storage.jobletrequestqueue.DatarouterJobletQueueDao;
import io.datarouter.joblet.storage.jobletrequestqueue.DatarouterJobletQueueDao.DatarouterJobletQueueDaoParams;
import io.datarouter.joblet.test.SleepingJoblet;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletTypeFactory;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.navigation.DatarouterNavBarCategory;
import io.datarouter.web.navigation.NavBarItem;

public class DatarouterJobletPlugin extends BaseJobletPlugin{

	private final List<JobletType<?>> jobletTypes;
	private final Class<? extends JobletExternalLinkBuilder> externalLinkBuilderClass;

	private DatarouterJobletPlugin(DatarouterJobletDaoModule daosModule){
		this(null, null, daosModule);
	}

	private DatarouterJobletPlugin(
			List<JobletType<?>> jobletTypes,
			Class<? extends JobletExternalLinkBuilder> externalLinkBuilderClass,
			DatarouterJobletDaoModule daosModule){
		this.jobletTypes = jobletTypes;
		this.externalLinkBuilderClass = externalLinkBuilderClass;
		addUnorderedAppListener(DatarouterJobletAppListener.class);
		addDatarouterNavBarItem(new NavBarItem(DatarouterNavBarCategory.JOBS,
				new DatarouterJobletPaths().datarouter.joblets.list, "Joblets"));
		addUnorderedRouteSet(DatarouterJobletRouteSet.class);
		addTriggerGroup(DatarouterJobletTriggerGroup.class);
		addSettingRoot(DatarouterJobletSettingRoot.class);
		setDaosModuleBuilder(daosModule);
		addJobletType(SleepingJoblet.JOBLET_TYPE);
	}

	@Override
	public String getName(){
		return "DatarouterJoblet";
	}

	@Override
	public void configure(){
		bindActualInstance(JobletTypeFactory.class, new JobletTypeFactory(jobletTypes));
		bindDefault(JobletExternalLinkBuilder.class, externalLinkBuilderClass);
	}

	public static class DatarouterJobletPluginBuilder{

		private final ClientId defaultClientId;
		private final ClientId defaultQueueClientId;

		private List<JobletType<?>> jobletTypes = new ArrayList<>();
		private Class<? extends JobletExternalLinkBuilder> externalLinkBuilderClass
				= NoOpJobletExternalLinkBuilder.class;
		private DatarouterJobletDaoModule daoModule;

		public DatarouterJobletPluginBuilder(ClientId defaultClientId, ClientId defaultQueueClientId){
			this.defaultClientId = defaultClientId;
			this.defaultQueueClientId = defaultQueueClientId;
		}

		public DatarouterJobletPluginBuilder setDaoModule(DatarouterJobletDaoModule daoModule){
			this.daoModule = daoModule;
			return this;
		}

		public DatarouterJobletPluginBuilder setJobletTypes(List<JobletType<?>> jobletTypes){
			this.jobletTypes.addAll(jobletTypes);
			return this;
		}

		public DatarouterJobletPluginBuilder setExternalLinkBuilderClass(
				Class<? extends JobletExternalLinkBuilder> externalLinkBuilderClass){
			this.externalLinkBuilderClass = externalLinkBuilderClass;
			return this;
		}

		public DatarouterJobletPlugin getSimplePluginData(){
			return new DatarouterJobletPlugin(daoModule == null
					? new DatarouterJobletDaoModule(defaultClientId, defaultQueueClientId, defaultClientId)
					: daoModule);
		}

		public DatarouterJobletPlugin build(){
			return new DatarouterJobletPlugin(
					jobletTypes,
					externalLinkBuilderClass,
					daoModule == null
							? new DatarouterJobletDaoModule(defaultClientId, defaultQueueClientId, defaultClientId)
							: daoModule);
		}

	}

	public static class DatarouterJobletDaoModule extends DaosModuleBuilder{

		private final ClientId datarouterJobletDataClientId;
		private final ClientId datarouterJobletQueueClientId;
		private final ClientId datarouterJobletRequestClientId;

		public DatarouterJobletDaoModule(
				ClientId datarouterJobletDataClientId,
				ClientId datarouterJobletQueueClientId,
				ClientId datarouterJobletRequestClientId){
			this.datarouterJobletDataClientId = datarouterJobletDataClientId;
			this.datarouterJobletQueueClientId = datarouterJobletQueueClientId;
			this.datarouterJobletRequestClientId = datarouterJobletRequestClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(
					DatarouterJobletDataDao.class,
					DatarouterJobletQueueDao.class,
					DatarouterJobletRequestDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterJobletRequestDaoParams.class)
					.toInstance(new DatarouterJobletRequestDaoParams(datarouterJobletRequestClientId));
			bind(DatarouterJobletDataDaoParams.class)
					.toInstance(new DatarouterJobletDataDaoParams(datarouterJobletDataClientId));
		}

		@Provides
		public DatarouterJobletQueueDaoParams getDatarouterJobletQueueRouterParams(){
			return new DatarouterJobletQueueDaoParams(datarouterJobletQueueClientId);
		}

	}

}
