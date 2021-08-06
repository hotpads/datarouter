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
package io.datarouter.joblet.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.joblet.DatarouterJobletAppListener;
import io.datarouter.joblet.enums.JobletQueueMechanism;
import io.datarouter.joblet.metriclink.AppJobletMetricLinkPage;
import io.datarouter.joblet.metriclink.DatarouterJobletMetricLinkPage;
import io.datarouter.joblet.nav.JobletExternalLinkBuilder;
import io.datarouter.joblet.nav.JobletExternalLinkBuilder.NoOpJobletExternalLinkBuilder;
import io.datarouter.joblet.queue.JobletRequestSelector;
import io.datarouter.joblet.queue.JobletSelectorRegistry;
import io.datarouter.joblet.queue.QueueJobletRequestSelector;
import io.datarouter.joblet.service.FailedJobletDailyDigest;
import io.datarouter.joblet.service.OldJobletDailyDigest;
import io.datarouter.joblet.setting.BaseJobletPlugin;
import io.datarouter.joblet.setting.DatarouterJobletSettingRoot;
import io.datarouter.joblet.storage.jobletdata.DatarouterJobletDataDao;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequestqueue.DatarouterJobletQueueDao;
import io.datarouter.joblet.test.SleepingJoblet;
import io.datarouter.joblet.type.JobletType;
import io.datarouter.joblet.type.JobletTypeFactory;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterJobletPlugin extends BaseJobletPlugin{

	private final List<JobletType<?>> jobletTypes;
	private final JobletSelectorRegistry jobletSelectorRegistry;
	private final Class<? extends JobletExternalLinkBuilder> externalLinkBuilderClass;

	private DatarouterJobletPlugin(List<Pair<Class<? extends Dao>,List<ClientId>>> daosAndClients){
		this(null, null, null, daosAndClients);
	}

	private DatarouterJobletPlugin(
			List<JobletType<?>> jobletTypes,
			JobletSelectorRegistry jobletSelectorRegistry,
			Class<? extends JobletExternalLinkBuilder> externalLinkBuilderClass,
			List<Pair<Class<? extends Dao>,List<ClientId>>> daosAndClients){
		this.jobletTypes = jobletTypes;
		this.jobletSelectorRegistry = jobletSelectorRegistry;
		this.externalLinkBuilderClass = externalLinkBuilderClass;
		addAppListener(DatarouterJobletAppListener.class);
		addDatarouterNavBarItem(DatarouterNavBarCategory.JOBS, new DatarouterJobletPaths().datarouter.joblets.list,
				"Joblets");
		addRouteSet(DatarouterJobletRouteSet.class);
		addTriggerGroup(DatarouterJobletTriggerGroup.class);
		addSettingRoot(DatarouterJobletSettingRoot.class);
		setDaosModule(daosAndClients);
		addJobletType(SleepingJoblet.JOBLET_TYPE);
		addTestable(DatarouterJobletBootstrapIntegrationService.class);
		addDatarouterGithubDocLink("datarouter-joblet");
		addDailyDigest(FailedJobletDailyDigest.class);
		addDailyDigest(OldJobletDailyDigest.class);
		addMetricLinkPages(AppJobletMetricLinkPage.class);
		addMetricLinkPages(DatarouterJobletMetricLinkPage.class);
	}

	@Override
	public void configure(){
		bindActualInstance(JobletTypeFactory.class, new JobletTypeFactory(jobletTypes));
		bind(JobletSelectorRegistry.class).toInstance(jobletSelectorRegistry);
		bindDefault(JobletExternalLinkBuilder.class, externalLinkBuilderClass);
	}

	public static class DatarouterJobletPluginBuilder{

		private List<ClientId> datarouterJobletDataClientId;
		private List<ClientId> datarouterJobletQueueClientId;
		private List<ClientId> datarouterJobletRequestClientId;
		private final List<JobletType<?>> jobletTypes = new ArrayList<>();
		private final JobletSelectorRegistry jobletSelectorRegistry = new JobletSelectorRegistry();
		private Class<? extends JobletExternalLinkBuilder> externalLinkBuilderClass
				= NoOpJobletExternalLinkBuilder.class;

		public DatarouterJobletPluginBuilder(List<ClientId> defaultClientId, List<ClientId> defaultQueueClientId){
			this.datarouterJobletDataClientId = defaultClientId;
			this.datarouterJobletQueueClientId = defaultQueueClientId;
			this.datarouterJobletRequestClientId = defaultClientId;
			withSelector(
					JobletQueueMechanism.QUEUE.getPersistentString(),
					QueueJobletRequestSelector.class);
		}

		// == the three following methods allow you to customize client id for each dao
		public void setDatarouterJobletDataClientId(List<ClientId> datarouterJobletDataClientId){
			this.datarouterJobletDataClientId = datarouterJobletDataClientId;
		}

		public void setDatarouterJobletQueueClientId(List<ClientId> datarouterJobletQueueClientId){
			this.datarouterJobletQueueClientId = datarouterJobletQueueClientId;
		}

		public void setDatarouterJobletRequestClientId(List<ClientId> datarouterJobletRequestClientId){
			this.datarouterJobletRequestClientId = datarouterJobletRequestClientId;
		}
		// ==

		public DatarouterJobletPluginBuilder setJobletTypes(List<JobletType<?>> jobletTypes){
			this.jobletTypes.addAll(jobletTypes);
			return this;
		}

		public DatarouterJobletPluginBuilder withSelector(
				String name,
				Class<? extends JobletRequestSelector> selectorClass){
			jobletSelectorRegistry.register(name, selectorClass);
			return this;
		}

		public DatarouterJobletPluginBuilder withSelectorTypes(
				List<Pair<String,Class<? extends JobletRequestSelector>>> selectorTypes){
			selectorTypes.forEach(pair -> jobletSelectorRegistry.register(pair.getLeft(), pair.getRight()));
			return this;
		}

		public DatarouterJobletPluginBuilder setExternalLinkBuilderClass(
				Class<? extends JobletExternalLinkBuilder> externalLinkBuilderClass){
			this.externalLinkBuilderClass = externalLinkBuilderClass;
			return this;
		}

		public DatarouterJobletPlugin getSimplePluginData(){
			return new DatarouterJobletPlugin(makeDaosAndClients());
		}

		public DatarouterJobletPlugin build(){
			return new DatarouterJobletPlugin(
					jobletTypes,
					jobletSelectorRegistry,
					externalLinkBuilderClass,
					makeDaosAndClients());
		}

		private List<Pair<Class<? extends Dao>,List<ClientId>>> makeDaosAndClients(){
			return List.of(
					new Pair<>(DatarouterJobletDataDao.class, datarouterJobletDataClientId),
					new Pair<>(DatarouterJobletQueueDao.class, datarouterJobletQueueClientId),
					new Pair<>(DatarouterJobletRequestDao.class, datarouterJobletRequestClientId));
		}

	}

}
