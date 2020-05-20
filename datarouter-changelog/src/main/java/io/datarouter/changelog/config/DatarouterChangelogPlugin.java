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
package io.datarouter.changelog.config;

import java.util.List;

import io.datarouter.changelog.service.ChangelogRecorderService;
import io.datarouter.changelog.storage.ChangelogDao;
import io.datarouter.changelog.storage.ChangelogDao.ChangelogDaoParams;
import io.datarouter.instrumentation.changelog.ChangelogPublisher;
import io.datarouter.instrumentation.changelog.ChangelogPublisher.NoOpChangelogPublisher;
import io.datarouter.instrumentation.changelog.ChangelogRecorder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterChangelogPlugin extends BaseWebPlugin{

	private static final DatarouterChangelogPaths PATHS = new DatarouterChangelogPaths();

	private final Class<? extends ChangelogPublisher> changelogPublisher;

	private DatarouterChangelogPlugin(
			Class<? extends ChangelogPublisher> changelogPublisher,
			DatarouterChangelogDaosModule daosModule){
		this.changelogPublisher = changelogPublisher;
		addSettingRoot(DatarouterChangelogSettingRoot.class);
		addRouteSet(DatarouterChangelogRouteSet.class);
		setDaosModule(daosModule);
		addDatarouterNavBarItem(DatarouterNavBarCategory.CHANGELOG, PATHS.datarouter.changelog.view, "View Changelog");
		addDatarouterNavBarItem(DatarouterNavBarCategory.CHANGELOG, PATHS.datarouter.changelog.insert, "Add");
	}

	@Override
	public String getName(){
		return "DatarouterChangelog";
	}

	@Override
	protected void configure(){
		bind(ChangelogPublisher.class).to(changelogPublisher);
		bindActual(ChangelogRecorder.class, ChangelogRecorderService.class);
	}

	public static class DatarouterChangelogPluginBuilder{

		private final ClientId defaultClientId;

		private Class<? extends ChangelogPublisher> changelogPublisher = NoOpChangelogPublisher.class;
		private DatarouterChangelogDaosModule daosModule;

		public DatarouterChangelogPluginBuilder(ClientId defaultClientId){
			this.defaultClientId = defaultClientId;
		}

		public DatarouterChangelogPluginBuilder enableChangelogPublishing(
				Class<? extends ChangelogPublisher> changelogPublisher){
			this.changelogPublisher = changelogPublisher;
			return this;
		}

		public DatarouterChangelogPluginBuilder setDaoModule(ClientId changelogClientId){
			this.daosModule = new DatarouterChangelogDaosModule(changelogClientId);
			return this;
		}

		public DatarouterChangelogPlugin build(){
			return new DatarouterChangelogPlugin(
					changelogPublisher,
					daosModule == null ? new DatarouterChangelogDaosModule(defaultClientId) : daosModule);
		}

	}

	public static class DatarouterChangelogDaosModule extends DaosModuleBuilder{

		private final ClientId changelogClientId;

		public DatarouterChangelogDaosModule(ClientId changelogClientId){
			this.changelogClientId = changelogClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(ChangelogDao.class);
		}

		@Override
		public void configure(){
			bind(ChangelogDaoParams.class).toInstance(new ChangelogDaoParams(changelogClientId));
		}

	}

}
