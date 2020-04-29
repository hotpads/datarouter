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
package io.datarouter.clustersetting.config;

import java.util.List;

import com.google.inject.name.Names;

import io.datarouter.clustersetting.ClusterSettingFinder;
import io.datarouter.clustersetting.listener.SettingNodeValidationAppListener;
import io.datarouter.clustersetting.storage.clustersetting.DatarouterClusterSettingDao;
import io.datarouter.clustersetting.storage.clustersetting.DatarouterClusterSettingDao.DatarouterClusterSettingDaoParams;
import io.datarouter.clustersetting.storage.clustersettinglog.DatarouterClusterSettingLogDao;
import io.datarouter.clustersetting.storage.clustersettinglog.DatarouterClusterSettingLogDao.DatarouterClusterSettingLogDaoParams;
import io.datarouter.instrumentation.changelog.ChangelogPublisher;
import io.datarouter.instrumentation.changelog.ChangelogPublisher.NoOpChangelogPublisher;
import io.datarouter.job.config.BaseJobPlugin;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.storage.setting.SettingFinder;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterClusterSettingPlugin extends BaseJobPlugin{

	public static final String NAMED_Changelog = "DatarouterClusterSettingChangelog";

	private final Class<? extends ChangelogPublisher> changelogPublisher;

	private DatarouterClusterSettingPlugin(
			DatarouterClusterSettingDaoModule daosModuleBuilder,
			Class<? extends ChangelogPublisher> changelogPublisher){
		this.changelogPublisher = changelogPublisher;

		addSettingRoot(DatarouterClusterSettingRoot.class);
		addRouteSet(DatarouterClusterSettingRouteSet.class);
		addAppListener(SettingNodeValidationAppListener.class);
		addTriggerGroup(DatarouterClusterSettingTriggerGroup.class);
		String browseSettings = new DatarouterClusterSettingPaths().datarouter.settings.toSlashedString()
				+ "?submitAction=browseSettings";
		String settingLogs = new DatarouterClusterSettingPaths().datarouter.settings.toSlashedString()
				+ "?submitAction=logsForAll";
		addDatarouterNavBarItem(DatarouterNavBarCategory.SETTINGS, browseSettings, "Browse Settings");
		addDatarouterNavBarItem(DatarouterNavBarCategory.SETTINGS, settingLogs, "Setting logs");
		addDatarouterNavBarItem(DatarouterNavBarCategory.SETTINGS,
				new DatarouterClusterSettingPaths().datarouter.settings, "Custom Settings");
		setDaosModule(daosModuleBuilder);
	}

	@Override
	public String getName(){
		return "DatarouterClusterSetting";
	}

	@Override
	protected void configure(){
		bindActual(SettingFinder.class, ClusterSettingFinder.class);
		bind(ChangelogPublisher.class)
				.annotatedWith(Names.named(NAMED_Changelog))
				.to(changelogPublisher);
	}

	public static class DatarouterClusterSettingPluginBuilder{

		private final ClientId defaultClientId;

		private DatarouterClusterSettingDaoModule daoModule;
		private Class<? extends ChangelogPublisher> changelogPublisher = NoOpChangelogPublisher.class;

		public DatarouterClusterSettingPluginBuilder(ClientId defaultClientId){
			this.defaultClientId = defaultClientId;
		}

		public DatarouterClusterSettingPluginBuilder setDaoModule(DatarouterClusterSettingDaoModule module){
			this.daoModule = module;
			return this;
		}

		public DatarouterClusterSettingPluginBuilder enableChangelogPublishing(
				Class<? extends ChangelogPublisher> changelogPublisher){
			this.changelogPublisher = changelogPublisher;
			return this;
		}

		public DatarouterClusterSettingPlugin build(){
			return new DatarouterClusterSettingPlugin(daoModule == null
					? new DatarouterClusterSettingDaoModule(defaultClientId, defaultClientId)
					: daoModule,
					changelogPublisher);
		}

	}

	public static class DatarouterClusterSettingDaoModule extends DaosModuleBuilder{

		private final ClientId datarouterClusterSettingClientId;
		private final ClientId datarouterClusterSettingLogClientId;

		public DatarouterClusterSettingDaoModule(
				ClientId datarouterClusterSettingClientId,
				ClientId datarouterClusterSettingLogClientId){
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
