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

import io.datarouter.storage.client.ClientOptionsFactory;
import io.datarouter.storage.config.schema.SchemaUpdateOptionsFactory;
import io.datarouter.storage.config.setting.DatarouterSettingOverrides;
import io.datarouter.storage.config.setting.DatarouterStorageSettingRoot;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaoClasses;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.storage.servertype.ServerTypeDetector.NoOpServerTypeDetector;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.storage.setting.SettingRootsSupplier;
import io.datarouter.storage.setting.SettingRootsSupplier.SettingRoots;

public class DatarouterStoragePlugin extends BaseStoragePlugin{

	private final ServerTypes serverTypes;
	private final Class<? extends ServerTypeDetector> serverTypeDetectorClass;
	private final DatarouterProperties datarouterProperties;
	private final Class<? extends DatarouterSettingOverrides> settingOverridesClass;
	private final SettingRoots settingRoots;
	private final Class<? extends ClientOptionsFactory> clientOptionsFactoryClass;
	private final Class<? extends SchemaUpdateOptionsFactory> schemaUpdateOptionsFactoryClass;
	private final List<Class<? extends Dao>> daoClasses;

	// only used to get simple data from plugin
	private DatarouterStoragePlugin(){
		this(null, null, null, null, null, null, null, null);
	}

	private DatarouterStoragePlugin(
			ServerTypes serverTypes,
			Class<? extends ServerTypeDetector> serverTypeDetectorClass,
			DatarouterProperties datarouterProperties,
			Class<? extends DatarouterSettingOverrides> settingOverridesClass,
			SettingRoots settingRoots,
			Class<? extends ClientOptionsFactory> clientOptionsFactoryClass,
			Class<? extends SchemaUpdateOptionsFactory> schemaUpdateOptionsFactoryClass,
			List<Class<? extends Dao>> daoClasses){
		addSettingRoot(DatarouterStorageSettingRoot.class);
		this.serverTypes = serverTypes;
		this.serverTypeDetectorClass = serverTypeDetectorClass;
		this.datarouterProperties = datarouterProperties;
		this.settingOverridesClass = settingOverridesClass;
		this.settingRoots = settingRoots;
		this.clientOptionsFactoryClass = clientOptionsFactoryClass;
		this.schemaUpdateOptionsFactoryClass = schemaUpdateOptionsFactoryClass;
		this.daoClasses = daoClasses;
	}

	@Override
	public String getName(){
		return "DatarouterStorage";
	}

	@Override
	public void configure(){
		bind(ServerTypes.class).toInstance(serverTypes);
		bindActual(ServerTypeDetector.class, serverTypeDetectorClass);
		bindActualInstance(DatarouterProperties.class, datarouterProperties);
		if(settingOverridesClass != null){
			bind(settingOverridesClass).asEagerSingleton(); // allow overriders in tests;
		}
		if(settingRoots != null){
			bindActualInstance(SettingRootsSupplier.class, settingRoots);
		}
		if(clientOptionsFactoryClass != null){
			bindActual(ClientOptionsFactory.class, clientOptionsFactoryClass);
		}
		if(schemaUpdateOptionsFactoryClass != null){
			bindActual(SchemaUpdateOptionsFactory.class, schemaUpdateOptionsFactoryClass);
		}
		bind(DaoClasses.class).toInstance(new DaoClasses(daoClasses));
	}

	public static class DatarouterStoragePluginBuilder{

		private final ServerTypes serverTypes;
		private final DatarouterProperties datarouterProperties;

		private Class<? extends ServerTypeDetector> serverTypeDetectorClass = NoOpServerTypeDetector.class;
		private Class<? extends DatarouterSettingOverrides> settingOverridesClass;
		private SettingRoots settingRoots;
		private Class<? extends ClientOptionsFactory> clientOptionsFactoryClass;
		private Class<? extends SchemaUpdateOptionsFactory> schemaUpdateOptionsFactoryClass;
		private List<Class<? extends Dao>> daoClasses = new ArrayList<>();

		public DatarouterStoragePluginBuilder(ServerTypes serverTypes, DatarouterProperties datarouterProperties){
			this.serverTypes = serverTypes;
			this.datarouterProperties = datarouterProperties;
		}

		public DatarouterStoragePluginBuilder setServerTypeDetector(
				Class<? extends ServerTypeDetector> serveTypeDetectorClass){
			this.serverTypeDetectorClass = serveTypeDetectorClass;
			return this;
		}

		public DatarouterStoragePluginBuilder setSettingOverridesClass(
				Class<? extends DatarouterSettingOverrides> settingOverridesClass){
			this.settingOverridesClass = settingOverridesClass;
			return this;
		}

		public DatarouterStoragePluginBuilder setSettingRootsClass(SettingRoots settingRoots){
			this.settingRoots = settingRoots;
			return this;
		}

		public DatarouterStoragePluginBuilder setClientOptionsFactoryClass(
				Class<? extends ClientOptionsFactory> clientOptionsFactoryClass){
			this.clientOptionsFactoryClass = clientOptionsFactoryClass;
			return this;
		}

		public DatarouterStoragePluginBuilder setSchemaUpdateOptionsFactoryClass(
				Class<? extends SchemaUpdateOptionsFactory> schemaUpdateOptionsFactoryClass){
			this.schemaUpdateOptionsFactoryClass = schemaUpdateOptionsFactoryClass;
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

		public DatarouterStoragePlugin getSimplePluginData(){
			return new DatarouterStoragePlugin();
		}

		public DatarouterStoragePlugin build(){
			return new DatarouterStoragePlugin(
					serverTypes,
					serverTypeDetectorClass,
					datarouterProperties,
					settingOverridesClass,
					settingRoots,
					clientOptionsFactoryClass,
					schemaUpdateOptionsFactoryClass,
					daoClasses);
		}

	}

}
