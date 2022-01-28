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

import java.util.List;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.joblet.DatarouterJobletAppListener;
import io.datarouter.joblet.enums.JobletQueueMechanism;
import io.datarouter.joblet.metriclink.AppJobletMetricLinkPage;
import io.datarouter.joblet.metriclink.DatarouterJobletMetricLinkPage;
import io.datarouter.joblet.queue.QueueJobletRequestSelector;
import io.datarouter.joblet.service.FailedJobletDailyDigest;
import io.datarouter.joblet.service.OldJobletDailyDigest;
import io.datarouter.joblet.setting.DatarouterJobletSettingRoot;
import io.datarouter.joblet.storage.jobletdata.DatarouterJobletDataDao;
import io.datarouter.joblet.storage.jobletrequest.DatarouterJobletRequestDao;
import io.datarouter.joblet.storage.jobletrequestqueue.DatarouterJobletQueueDao;
import io.datarouter.joblet.test.SleepingJoblet;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.util.tuple.Pair;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterJobletPlugin extends BaseWebPlugin{

	private DatarouterJobletPlugin(
			List<Pair<Class<? extends Dao>,List<ClientId>>> daosAndClients){
		addAppListener(DatarouterJobletAppListener.class);
		addDatarouterNavBarItem(DatarouterNavBarCategory.JOBS, new DatarouterJobletPaths().datarouter.joblets.list,
				"Joblets");
		addRouteSet(DatarouterJobletRouteSet.class);
		addPluginEntry(BaseTriggerGroup.KEY, DatarouterJobletTriggerGroup.class);
		addSettingRoot(DatarouterJobletSettingRoot.class);
		setDaosModule(daosAndClients);
		addPluginEntry(SleepingJoblet.JOBLET_TYPE);
		addTestable(DatarouterJobletBootstrapIntegrationService.class);
		addDatarouterGithubDocLink("datarouter-joblet");
		addDailyDigest(FailedJobletDailyDigest.class);
		addDailyDigest(OldJobletDailyDigest.class);
		addMetricLinkPages(AppJobletMetricLinkPage.class);
		addMetricLinkPages(DatarouterJobletMetricLinkPage.class);
		// TODO support overriding
		addPluginEntry(JobletQueueMechanism.QUEUE.getKey(), QueueJobletRequestSelector.class);
	}

	public static class DatarouterJobletPluginBuilder{

		private List<ClientId> datarouterJobletDataClientId;
		private List<ClientId> datarouterJobletQueueClientId;
		private List<ClientId> datarouterJobletRequestClientId;

		public DatarouterJobletPluginBuilder(List<ClientId> defaultClientId, List<ClientId> defaultQueueClientId){
			this.datarouterJobletDataClientId = defaultClientId;
			this.datarouterJobletQueueClientId = defaultQueueClientId;
			this.datarouterJobletRequestClientId = defaultClientId;
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

		public DatarouterJobletPlugin build(){
			return new DatarouterJobletPlugin(
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
