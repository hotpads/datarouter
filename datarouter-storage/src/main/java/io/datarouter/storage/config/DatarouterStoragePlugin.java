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
package io.datarouter.storage.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.plugin.PluginConfiguration;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptionsFactory;
import io.datarouter.storage.config.schema.SchemaUpdateOptionsFactory;
import io.datarouter.storage.config.setting.DatarouterSettingOverrides;
import io.datarouter.storage.config.setting.DatarouterStorageSettingRoot;
import io.datarouter.storage.config.storage.clusterschemaupdatelock.DatarouterClusterSchemaUpdateLockDao;
import io.datarouter.storage.config.storage.clusterschemaupdatelock.DatarouterClusterSchemaUpdateLockDao.DatarouterClusterSchemaUpdateLockDaoParams;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaoClasses;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.storage.servertype.ServerTypeDetector.NoOpServerTypeDetector;
import io.datarouter.storage.servertype.ServerTypes;
import io.datarouter.storage.setting.SettingRootsSupplier;

public class DatarouterStoragePlugin extends BaseStoragePlugin{

	private final ServerTypes serverTypes;
	private final Class<? extends ServerTypeDetector> serverTypeDetectorClass;
	private final Class<? extends DatarouterSettingOverrides> settingOverridesClass;
	private final SettingRootsSupplier settingRoots;
	private final Class<? extends ClientOptionsFactory> clientOptionsFactoryClass;
	private final Class<? extends SchemaUpdateOptionsFactory> schemaUpdateOptionsFactoryClass;
	private final List<Class<? extends Dao>> daoClasses;
	private final DatarouterStorageDaosModule daosModule;

	private final Map<PluginConfigKey<?>,Class<? extends PluginConfigValue<?>>> classSingle;
	private final Map<PluginConfigKey<?>,List<Class<? extends PluginConfigValue<?>>>> classList;
	private final Map<PluginConfigKey<?>,PluginConfigValue<?>> instanceSingle;
	private final Map<PluginConfigKey<?>,List<PluginConfigValue<?>>> instanceList;

	// only used to get simple data from plugin
	private DatarouterStoragePlugin(DatarouterStorageDaosModule daosModule){
		this(null, null, null, null, null, null, null, daosModule, null, null, null, null);
	}

	private DatarouterStoragePlugin(
			ServerTypes serverTypes,
			Class<? extends ServerTypeDetector> serverTypeDetectorClass,
			Class<? extends DatarouterSettingOverrides> settingOverridesClass,
			SettingRootsSupplier settingRoots,
			Class<? extends ClientOptionsFactory> clientOptionsFactoryClass,
			Class<? extends SchemaUpdateOptionsFactory> schemaUpdateOptionsFactoryClass,
			List<Class<? extends Dao>> daoClasses,
			DatarouterStorageDaosModule daosModule,

			Map<PluginConfigKey<?>,Class<? extends PluginConfigValue<?>>> classSingle,
			Map<PluginConfigKey<?>,List<Class<? extends PluginConfigValue<?>>>> classList,
			Map<PluginConfigKey<?>,PluginConfigValue<?>> instanceSingle,
			Map<PluginConfigKey<?>,List<PluginConfigValue<?>>> instanceList){
		this.daosModule = daosModule;
		this.serverTypes = serverTypes;
		this.serverTypeDetectorClass = serverTypeDetectorClass;
		this.settingOverridesClass = settingOverridesClass;
		this.settingRoots = settingRoots;
		this.clientOptionsFactoryClass = clientOptionsFactoryClass;
		this.schemaUpdateOptionsFactoryClass = schemaUpdateOptionsFactoryClass;
		this.daoClasses = daoClasses;

		this.classSingle = classSingle;
		this.classList = classList;
		this.instanceSingle = instanceSingle;
		this.instanceList = instanceList;

		addSettingRoot(DatarouterStorageSettingRoot.class);
		setDaosModule(daosModule);
	}

	public List<Class<? extends Dao>> getDatarouterStorageDaoClasses(){
		return daosModule.getDaoClasses();
	}

	@Override
	public void configure(){
		bind(ServerTypes.class).toInstance(serverTypes);
		bindActual(ServerTypeDetector.class, serverTypeDetectorClass);
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

		bindActualInstance(PluginConfiguration.class, new PluginConfiguration(
				classSingle,
				classList,
				instanceSingle,
				instanceList));
	}

	public static class DatarouterStoragePluginBuilder{

		private final ServerTypes serverTypes;
		private final List<ClientId> defaultClientIds;

		private Class<? extends ServerTypeDetector> serverTypeDetectorClass = NoOpServerTypeDetector.class;
		private Class<? extends DatarouterSettingOverrides> settingOverridesClass;
		private SettingRootsSupplier settingRoots;
		private Class<? extends ClientOptionsFactory> clientOptionsFactoryClass;
		private Class<? extends SchemaUpdateOptionsFactory> schemaUpdateOptionsFactoryClass;
		private List<Class<? extends Dao>> daoClasses = new ArrayList<>();

		private Map<PluginConfigKey<?>,Class<? extends PluginConfigValue<?>>> classSingle = new HashMap<>();
		private Map<PluginConfigKey<?>,List<Class<? extends PluginConfigValue<?>>>> classList = new HashMap<>();
		private Map<PluginConfigKey<?>,PluginConfigValue<?>> instanceSingle = new HashMap<>();
		private Map<PluginConfigKey<?>,List<PluginConfigValue<?>>> instanceList = new HashMap<>();


		public DatarouterStoragePluginBuilder(
				ServerTypes serverTypes,
				List<ClientId> defaultClientIds){
			this.serverTypes = serverTypes;
			this.defaultClientIds = defaultClientIds;
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

		public DatarouterStoragePluginBuilder setSettingRootsClass(SettingRootsSupplier settingRoots){
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

		public DatarouterStoragePluginBuilder setPluginConfigsClassList(
				Map<PluginConfigKey<?>,List<Class<? extends PluginConfigValue<?>>>> configs){
			this.classList = configs;
			return this;
		}

		public DatarouterStoragePluginBuilder setPluginConfigsClassSingle(
				Map<PluginConfigKey<?>,Class<? extends PluginConfigValue<?>>> configs){
			this.classSingle = configs;
			return this;
		}

		public DatarouterStoragePluginBuilder setPluginConfigsInstanceList(
				Map<PluginConfigKey<?>,List<PluginConfigValue<?>>> configs){
			this.instanceList = configs;
			return this;
		}

		public DatarouterStoragePluginBuilder setPluginConfigsInstanceSingle(
				Map<PluginConfigKey<?>,PluginConfigValue<?>> configs){
			this.instanceSingle = configs;
			return this;
		}

		public DatarouterStoragePlugin getSimplePluginData(){
			return new DatarouterStoragePlugin(new DatarouterStorageDaosModule(defaultClientIds));
		}

		public DatarouterStoragePlugin build(){
			return new DatarouterStoragePlugin(
					serverTypes,
					serverTypeDetectorClass,
					settingOverridesClass,
					settingRoots,
					clientOptionsFactoryClass,
					schemaUpdateOptionsFactoryClass,
					daoClasses,
					new DatarouterStorageDaosModule(defaultClientIds),
					classSingle,
					classList,
					instanceSingle,
					instanceList);
		}

	}

	public static class DatarouterStorageDaosModule extends DaosModuleBuilder{

		private final List<ClientId> defaultClientIds;

		public DatarouterStorageDaosModule(List<ClientId> defaultClientId){
			this.defaultClientIds = defaultClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(DatarouterClusterSchemaUpdateLockDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterClusterSchemaUpdateLockDaoParams.class)
					.toInstance(new DatarouterClusterSchemaUpdateLockDaoParams(defaultClientIds));
		}
	}

}
