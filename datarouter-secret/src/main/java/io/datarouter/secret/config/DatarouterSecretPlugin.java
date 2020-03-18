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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.datarouter.secret.client.LocalStorageSecretClient.LocalStorageDefaultSecretValues;
import io.datarouter.secret.client.Secret;
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

public class DatarouterSecretPlugin extends BaseWebPlugin{

	private final Class<? extends SecretHandlerPermissions> secretHandlerPermissions;
	private final Class<? extends SecretClientSupplier> secretClientSupplier;
	private final Map<String,String> initialLocalStorageSecretValues;

	private DatarouterSecretPlugin(
			Class<? extends SecretHandlerPermissions> secretHandlerPermissions,
			Class<? extends SecretClientSupplier> secretClientSupplier,
			Map<String,String> initialLocalStorageSecretValues,
			DatarouterSecretDaoModule daosModuleBuilder){
		this.secretHandlerPermissions = secretHandlerPermissions;
		this.secretClientSupplier = secretClientSupplier;
		this.initialLocalStorageSecretValues = initialLocalStorageSecretValues;
		addRouteSet(DatarouterSecretRouteSet.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterNavBarItem(DatarouterNavBarCategory.SETTINGS, new DatarouterSecretPaths().datarouter.secrets,
				"Secret");
	}

	@Override
	public String getName(){
		return "DatarouterSecret";
	}

	@Override
	public void configure(){
		bindActual(SecretClientSupplier.class, secretClientSupplier);
		bindActual(SecretHandlerPermissions.class, secretHandlerPermissions);
		bindActualInstance(LocalStorageDefaultSecretValues.class, new LocalStorageDefaultSecretValues(
				initialLocalStorageSecretValues));
	}

	public static class DatarouterSecretPluginBuilder{

		private final ClientId defaultClientId;

		private Map<String,String> initialLocalStorageSecretValues = new HashMap<>();
		private DatarouterSecretDaoModule daoModule;
		private Class<? extends SecretHandlerPermissions> secretHandlerPermissions = NoOpSecretHandlerPermissions.class;
		private Class<? extends SecretClientSupplier> secretClientSupplier = NoOpSecretClientSupplier.class;

		public DatarouterSecretPluginBuilder(ClientId defaultClientId){
			this.defaultClientId = defaultClientId;
		}

		public DatarouterSecretPluginBuilder setDaoModule(DatarouterSecretDaoModule daoModule){
			this.daoModule = daoModule;
			return this;
		}

		public DatarouterSecretPluginBuilder setSecretHandlerPermissions(
				Class<? extends SecretHandlerPermissions> secretHandlerPermissions){
			this.secretHandlerPermissions = secretHandlerPermissions;
			return this;
		}

		public DatarouterSecretPluginBuilder setSecretClientSupplier(
				Class<? extends SecretClientSupplier> secretClientSupplier){
			this.secretClientSupplier = secretClientSupplier;
			return this;
		}

		public DatarouterSecretPluginBuilder setInitialLocalStorageSecrets(Collection<Secret> secrets){
			initialLocalStorageSecretValues = secrets.stream()
					.collect(Collectors.toMap(Secret::getName, Secret::getValue));
			return this;
		}

		public DatarouterSecretPluginBuilder addLocalStorageSecret(Secret secret){
			initialLocalStorageSecretValues.put(secret.getName(), secret.getValue());
			return this;
		}

		public DatarouterSecretPluginBuilder addLocalStorageSecrets(Collection<Secret> secrets){

			initialLocalStorageSecretValues.putAll(secrets.stream()
					.collect(Collectors.toMap(Secret::getName, Secret::getValue)));
			return this;
		}

		public DatarouterSecretPlugin build(){
			return new DatarouterSecretPlugin(
					secretHandlerPermissions,
					secretClientSupplier,
					initialLocalStorageSecretValues,
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
			return List.of(DatarouterSecretOpRecordDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterSecretOpRecordDaoParams.class)
					.toInstance(new DatarouterSecretOpRecordDaoParams(datarouterSecretOpRecordDaoClientId));
		}

	}

}
