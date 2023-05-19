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
package io.datarouter.clustersetting.config;

import java.util.List;

import io.datarouter.clustersetting.ClusterSettingFinder;
import io.datarouter.clustersetting.listener.SettingNodeValidationAppListener;
import io.datarouter.clustersetting.service.ClusterSettingDailyDigest;
import io.datarouter.clustersetting.storage.clustersetting.DatarouterClusterSettingDao;
import io.datarouter.clustersetting.storage.clustersetting.DatarouterClusterSettingDao.DatarouterClusterSettingDaoParams;
import io.datarouter.clustersetting.storage.clustersettinglog.DatarouterClusterSettingLogDao;
import io.datarouter.clustersetting.storage.clustersettinglog.DatarouterClusterSettingLogDao.DatarouterClusterSettingLogDaoParams;
import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterClusterSettingPlugin extends BaseWebPlugin{

	public static final String NAME = "Cluster Settings";
	private static final DatarouterClusterSettingPaths PATHS = new DatarouterClusterSettingPaths();

	private DatarouterClusterSettingPlugin(DatarouterClusterSettingDaoModule daosModuleBuilder){
		addSettingRoot(DatarouterClusterSettingRoot.class);
		addRouteSet(DatarouterClusterSettingRouteSet.class);
		addAppListener(SettingNodeValidationAppListener.class);
		addPluginEntry(BaseTriggerGroup.KEY, DatarouterClusterSettingTriggerGroup.class);
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.CONFIGURATION,
				PATHS.datarouter.settings.toSlashedString() + "?submitAction=browseSettings",
				DatarouterClusterSettingPlugin.NAME + " - Browse");
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.CONFIGURATION,
				PATHS.datarouter.settings.browse.all,
				DatarouterClusterSettingPlugin.NAME + " - Browse V2");
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.CONFIGURATION,
				PATHS.datarouter.settings.log.all,
				DatarouterClusterSettingPlugin.NAME + " - Logs");
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.CONFIGURATION,
				PATHS.datarouter.settings.overrides.view,
				DatarouterClusterSettingPlugin.NAME + " - Overrides");
		addDynamicNavBarItem(DatarouterClusterSettingTagsDynamicNavBarMenuItem.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterGithubDocLink("datarouter-cluster-setting");
		addDailyDigest(ClusterSettingDailyDigest.class);
	}

	@Override
	protected void configure(){
		bindActual(SettingFinder.class, ClusterSettingFinder.class);
	}

	public static class DatarouterClusterSettingPluginBuilder{

		private final List<ClientId> defaultClientId;

		public DatarouterClusterSettingPluginBuilder(List<ClientId> defaultClientId){
			this.defaultClientId = defaultClientId;
		}

		public DatarouterClusterSettingPlugin build(){
			return new DatarouterClusterSettingPlugin(
					new DatarouterClusterSettingDaoModule(defaultClientId, defaultClientId));
		}

	}

	public static class DatarouterClusterSettingDaoModule extends DaosModuleBuilder{

		private final List<ClientId> datarouterClusterSettingClientId;
		private final List<ClientId> datarouterClusterSettingLogClientId;

		public DatarouterClusterSettingDaoModule(
				List<ClientId> datarouterClusterSettingClientId,
				List<ClientId> datarouterClusterSettingLogClientId){
			this.datarouterClusterSettingClientId = datarouterClusterSettingClientId;
			this.datarouterClusterSettingLogClientId = datarouterClusterSettingLogClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(
					DatarouterClusterSettingDao.class,
					DatarouterClusterSettingLogDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterClusterSettingDaoParams.class)
					.toInstance(new DatarouterClusterSettingDaoParams(datarouterClusterSettingClientId));
			bind(DatarouterClusterSettingLogDaoParams.class)
					.toInstance(new DatarouterClusterSettingLogDaoParams(datarouterClusterSettingLogClientId));
		}

	}

}
