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
package io.datarouter.secret.config;

import java.util.List;
import java.util.Map;

import io.datarouter.secret.client.LocalStorageSecretClient.LocalStorageDefaultSecretValues;
import io.datarouter.secret.client.SecretClientSupplier;
import io.datarouter.secret.client.SecretClientSupplier.NoOpSecretClientSupplier;
import io.datarouter.secret.handler.SecretHandlerPermissions;
import io.datarouter.secret.handler.SecretHandlerPermissions.NoOpSecretHandlerPermissions;
import io.datarouter.secret.storage.oprecord.DatarouterSecretOpRecordDao;
import io.datarouter.secret.storage.oprecord.DatarouterSecretOpRecordDao.DatarouterSecretOpRecordDaoParams;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;
import io.datarouter.web.navigation.NavBarItem;

public class DatarouterSecretPlugin extends BaseWebPlugin{

	private final Class<? extends SecretHandlerPermissions> secretHandlerPermissionsClass;
	private final Class<? extends SecretClientSupplier> secretClientSupplierClass;
	private final Map<String,String> initialLocalStorageSecretValues;

	private DatarouterSecretPlugin(
			Class<? extends SecretHandlerPermissions> secretHandlerPermissionsClass,
			Class<? extends SecretClientSupplier> secretClientSupplierClass,
			Map<String,String> initialLocalStorageSecretValues,
			DatarouterSecretDaoModule daosModuleBuilder){
		this.secretHandlerPermissionsClass = secretHandlerPermissionsClass;
		this.secretClientSupplierClass = secretClientSupplierClass;
		this.initialLocalStorageSecretValues = initialLocalStorageSecretValues;
		addUnorderedRouteSet(DatarouterSecretRouteSet.class);
		setDaosModuleBuilder(daosModuleBuilder);
		addDatarouterNavBarItem(new NavBarItem(DatarouterNavBarCategory.SETTINGS,
				new DatarouterSecretPaths().datarouter.secrets, "Secret"));
	}

	@Override
	public String getName(){
		return "DatarouterSecret";
	}

	@Override
	public void configure(){
		bindActual(SecretClientSupplier.class, secretClientSupplierClass);
		bindActual(SecretHandlerPermissions.class, secretHandlerPermissionsClass);
		bindActualInstance(LocalStorageDefaultSecretValues.class, new LocalStorageDefaultSecretValues(
				initialLocalStorageSecretValues));
	}

	public static class DatarouterSecretPluginBuilder{

		private final ClientId defaultClientId;
		private DatarouterSecretDaoModule daoModule;

		private Class<? extends SecretHandlerPermissions> secretHandlerPermissionsClass
				= NoOpSecretHandlerPermissions.class;
		private Class<? extends SecretClientSupplier> secretClientSupplierClass
				= NoOpSecretClientSupplier.class;
		private Map<String,String> initialLocalStorageSecretValues;

		public DatarouterSecretPluginBuilder(ClientId defaultClientId){
			this.defaultClientId = defaultClientId;
		}

		public DatarouterSecretPluginBuilder setDaoModule(DatarouterSecretDaoModule daoModule){
			this.daoModule = daoModule;
			return this;
		}

		public DatarouterSecretPluginBuilder setSecretHandlerPermissions(
				Class<? extends SecretHandlerPermissions> secretHandlerPermissionsClass){
			this.secretHandlerPermissionsClass = secretHandlerPermissionsClass;
			return this;
		}

		public DatarouterSecretPluginBuilder setSecretClientSupplier(
				Class<? extends SecretClientSupplier> secretClientSupplierClass){
			this.secretClientSupplierClass = secretClientSupplierClass;
			return this;
		}

		public DatarouterSecretPluginBuilder setInitialLocalStorageSecretValues(
				Map<String,String> initialLocalStorageSecretValues){
			this.initialLocalStorageSecretValues = initialLocalStorageSecretValues;
			return this;
		}

		public DatarouterSecretPlugin build(){
			return new DatarouterSecretPlugin(
					secretHandlerPermissionsClass,
					secretClientSupplierClass,
					initialLocalStorageSecretValues == null ? Map.of() : initialLocalStorageSecretValues,
					daoModule == null ? new DatarouterSecretDaoModule(defaultClientId) : daoModule);
		}

	}

	public static class DatarouterSecretDaoModule extends DaosModuleBuilder{

		private final ClientId datarouterSecretOpRecordDaoClientId;

		public DatarouterSecretDaoModule(ClientId datarouterSecretOpRecordDaoClientId){
			this.datarouterSecretOpRecordDaoClientId = datarouterSecretOpRecordDaoClientId;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(
					DatarouterSecretOpRecordDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterSecretOpRecordDaoParams.class)
					.toInstance(new DatarouterSecretOpRecordDaoParams(datarouterSecretOpRecordDaoClientId));
		}

	}

}
