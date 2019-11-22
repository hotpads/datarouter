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
package io.datarouter.storage.config;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.storage.config.setting.DatarouterStorageSettingRoot;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaoClasses;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.storage.servertype.ServerTypeDetector.NoOpServerTypeDetector;
import io.datarouter.storage.setting.AdditionalSettingRoots;
import io.datarouter.storage.setting.AdditionalSettingRootsSupplier;
import io.datarouter.storage.setting.SettingRoot;
import io.datarouter.storage.setting.SettingRootNameSupplier;

public class DatarouterStoragePlugin extends BasePlugin{

	private final ServerType serverType;
	private final Class<? extends ServerTypeDetector> serverTypeDetectorClass;
	private final Class<? extends DatarouterProperties> datarouterPropertiesClass;
	private final Class<? extends SettingRoot> settingRootClass;
	private final String settingRootName;
	private final Class<?> settingOverridesClass;
	private final AdditionalSettingRoots additionalSettingRoots;
	private final List<Class<? extends Dao>> daoClasses;

	private DatarouterStoragePlugin(
			ServerType serverType,
			Class<? extends ServerTypeDetector> serverTypeDetectorClass,
			Class<? extends DatarouterProperties> datarouterPropertiesClass,
			Class<? extends SettingRoot> settingRootClass,
			String settingRootName,
			Class<?> settingOverridesClass,
			AdditionalSettingRoots additionalSettingRoots,
			List<Class<? extends Dao>> daoClasses){
		addSettingRoot(DatarouterStorageSettingRoot.class);
		this.serverType = serverType;
		this.serverTypeDetectorClass = serverTypeDetectorClass;
		this.datarouterPropertiesClass = datarouterPropertiesClass;
		this.settingRootClass = settingRootClass;
		this.settingRootName = settingRootName;
		this.settingOverridesClass = settingOverridesClass;
		this.additionalSettingRoots = additionalSettingRoots;
		this.daoClasses = daoClasses;
	}

	@Override
	public void configure(){
		bind(ServerType.class).toInstance(serverType);
		bindActual(ServerTypeDetector.class, serverTypeDetectorClass);
		bind(DatarouterProperties.class).to(datarouterPropertiesClass);
		bind(SettingRoot.class).to(settingRootClass);
		bind(SettingRootNameSupplier.class).toInstance(() -> settingRootName);
		if(settingOverridesClass != null){
			bind(settingOverridesClass).asEagerSingleton(); // allow overriders in tests;
		}
		if(additionalSettingRoots != null){
			bindActualInstance(AdditionalSettingRootsSupplier.class, additionalSettingRoots);
		}
		bind(DaoClasses.class).toInstance(new DaoClasses(daoClasses));
	}

	public static class DatarouterStoragePluginBuilder{

		private final ServerType serverType;
		private final Class<? extends DatarouterProperties> datarouterPropertiesClass;
		private final Class<? extends SettingRoot> settingRootClass;
		private final String settingRootName;

		private Class<? extends ServerTypeDetector> serverTypeDetectorClass = NoOpServerTypeDetector.class;
		private Class<?> settingOverridesClass;
		private AdditionalSettingRoots additionalSettingRoots;
		private List<Class<? extends Dao>> daoClasses = new ArrayList<>();

		public DatarouterStoragePluginBuilder(ServerType serverType,
				Class<? extends DatarouterProperties> datarouterPropertiesClass,
				Class<? extends SettingRoot> settingRootClass,
				String settingRootName){
			this.serverType = serverType;
			this.datarouterPropertiesClass = datarouterPropertiesClass;
			this.settingRootClass = settingRootClass;
			this.settingRootName = settingRootName;
		}

		public DatarouterStoragePluginBuilder setServerTypeDetector(
				Class<? extends ServerTypeDetector> serveTypeDetectorClass){
			this.serverTypeDetectorClass = serveTypeDetectorClass;
			return this;
		}

		public DatarouterStoragePluginBuilder setSettingOverridesClass(Class<?> settingOverridesClass){
			this.settingOverridesClass = settingOverridesClass;
			return this;
		}

		public DatarouterStoragePluginBuilder setAdditionalSettingRootsClass(
				AdditionalSettingRoots additionalSettingRoots){
			this.additionalSettingRoots = additionalSettingRoots;
			return this;
		}

		public DatarouterStoragePluginBuilder addDao(Class<? extends Dao> dao){
			this.daoClasses.add(dao);
			return this;
		}

		public DatarouterStoragePluginBuilder addDaosClasses(List<Class<? extends Dao>> daos){
			this.daoClasses.addAll(daos);
			return this;
		}

		public DatarouterStoragePlugin build(){
			return new DatarouterStoragePlugin(
					serverType,
					serverTypeDetectorClass,
					datarouterPropertiesClass,
					settingRootClass,
					settingRootName,
					settingOverridesClass,
					additionalSettingRoots,
					daoClasses);
		}

	}

}
