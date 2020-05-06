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
package io.datarouter.loggerconfig.config;

import java.util.List;

import io.datarouter.job.config.BaseJobPlugin;
import io.datarouter.loggerconfig.storage.consoleappender.DatarouterConsoleAppenderDao;
import io.datarouter.loggerconfig.storage.consoleappender.DatarouterConsoleAppenderDao.DatarouterConsoleAppenderDaoParams;
import io.datarouter.loggerconfig.storage.fileappender.DatarouterFileAppenderDao;
import io.datarouter.loggerconfig.storage.fileappender.DatarouterFileAppenderDao.DatarouterFileAppenderDaoParams;
import io.datarouter.loggerconfig.storage.loggerconfig.DatarouterLoggerConfigDao;
import io.datarouter.loggerconfig.storage.loggerconfig.DatarouterLoggerConfigDao.DatarouterLoggerConfigDaoParams;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterLoggerConfigPlugin extends BaseJobPlugin{

	private DatarouterLoggerConfigPlugin(
			DatarouterLoggerConfigDaoModule daosModuleBuilder){

		addRouteSet(DatarouterLoggingConfigRouteSet.class);
		addSettingRoot(DatarouterLoggerConfigSettingRoot.class);
		addTriggerGroup(DatarouterLoggerConfigTriggerGroup.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterNavBarItem(DatarouterNavBarCategory.SETTINGS,
				new DatarouterLoggingConfigPaths().datarouter.logging, "Logger Config");
	}

	@Override
	public String getName(){
		return "DatarouterLoggerConfig";
	}

	public static class DatarouterLoggerConfigPluginBuilder{

		private final ClientId defaultClientId;

		private DatarouterLoggerConfigDaoModule daoModule;

		public DatarouterLoggerConfigPluginBuilder(ClientId defaultClientId){
			this.defaultClientId = defaultClientId;
		}

		public DatarouterLoggerConfigPluginBuilder setDaoModule(DatarouterLoggerConfigDaoModule daoModule){
			this.daoModule = daoModule;
			return this;
		}

		public DatarouterLoggerConfigPlugin build(){
			return new DatarouterLoggerConfigPlugin(daoModule == null
					? new DatarouterLoggerConfigDaoModule(defaultClientId, defaultClientId, defaultClientId)
					: daoModule);
		}

	}

	public static class DatarouterLoggerConfigDaoModule extends DaosModuleBuilder{

		private final ClientId datarouterConsoleAppenderClientId;
		private final ClientId datarouterFileAppenderClientId;
		private final ClientId datarouterLoggerConfigClientId;

		public DatarouterLoggerConfigDaoModule(
				ClientId datarouterConsoleAppenderClientId,
				ClientId datarouterFileAppenderClientId,
				ClientId datarouterLoggerConfigClientId){
			this.datarouterConsoleAppenderClientId = datarouterConsoleAppenderClientId;
			this.datarouterFileAppenderClientId = datarouterFileAppenderClientId;
			this.datarouterLoggerConfigClientId = datarouterLoggerConfigClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(
					DatarouterConsoleAppenderDao.class,
					DatarouterFileAppenderDao.class,
					DatarouterLoggerConfigDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterConsoleAppenderDaoParams.class)
					.toInstance(new DatarouterConsoleAppenderDaoParams(datarouterConsoleAppenderClientId));
			bind(DatarouterFileAppenderDaoParams.class)
					.toInstance(new DatarouterFileAppenderDaoParams(datarouterFileAppenderClientId));
			bind(DatarouterLoggerConfigDaoParams.class)
					.toInstance(new DatarouterLoggerConfigDaoParams(datarouterLoggerConfigClientId));
		}

	}

}
