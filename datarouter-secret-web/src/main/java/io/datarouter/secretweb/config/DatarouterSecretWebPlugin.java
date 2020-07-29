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
package io.datarouter.secretweb.config;

import java.util.List;

import io.datarouter.secret.config.DatarouterSecretPlugin;
import io.datarouter.secret.config.DatarouterSecretPlugin.DatarouterSecretPluginBuilder;
import io.datarouter.secretweb.service.DatarouterPropertiesAndServiceSecretNamespacer;
import io.datarouter.secretweb.service.DatarouterPropertiesLocalStorageConfig;
import io.datarouter.secretweb.service.DefaultHandlerSerializer;
import io.datarouter.secretweb.service.ServerTypeDetectorSecretStageDetector;
import io.datarouter.secretweb.storage.oprecord.DatarouterSecretOpRecordDao;
import io.datarouter.secretweb.storage.oprecord.DatarouterSecretOpRecordDao.DatarouterSecretOpRecordDaoParams;
import io.datarouter.secretweb.web.DefaultSecretHandlerPermissions;
import io.datarouter.secretweb.web.SecretHandlerPermissions;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterSecretWebPlugin extends BaseWebPlugin{

	private final DatarouterSecretPlugin basePlugin;
	private final Class<? extends SecretHandlerPermissions> secretHandlerPermissions;

	private DatarouterSecretWebPlugin(DatarouterSecretPlugin basePlugin,
			Class<? extends SecretHandlerPermissions> secretHandlerPermissions,
			DatarouterSecretDaoModule daosModuleBuilder){
		this.basePlugin = basePlugin;
		this.secretHandlerPermissions = secretHandlerPermissions;
		addRouteSet(DatarouterSecretRouteSet.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterNavBarItem(DatarouterNavBarCategory.SETTINGS, new DatarouterSecretPaths().datarouter.secrets,
				"Secrets");
	}

	@Override
	public String getName(){
		return "DatarouterSecret";
	}

	@Override
	public void configure(){
		install(basePlugin);
		bindActual(SecretHandlerPermissions.class, secretHandlerPermissions);
	}

	public abstract static class DatarouterSecretWebPluginBuilder<T extends DatarouterSecretWebPluginBuilder<T>>
	extends DatarouterSecretPluginBuilder<T>{

		private final ClientId defaultClientId;
		private Class<? extends SecretHandlerPermissions> secretHandlerPermissions = DefaultSecretHandlerPermissions
				.class;

		public static class DatarouterSecretWebPluginBuilderImpl
		extends DatarouterSecretWebPluginBuilder<DatarouterSecretWebPluginBuilderImpl>{

			public DatarouterSecretWebPluginBuilderImpl(ClientId defaultClientId){
				super(defaultClientId);
			}

			@Override
			protected DatarouterSecretWebPluginBuilderImpl getSelf(){
				return this;
			}

		}

		public DatarouterSecretWebPluginBuilder(ClientId defaultClientId){
			this.defaultClientId = defaultClientId;
			setSecretNamespacer(DatarouterPropertiesAndServiceSecretNamespacer.class);
			setSecretStageDetector(ServerTypeDetectorSecretStageDetector.class);
			setSecretOpRecorder(DatarouterSecretOpRecordDao.class);
			setLocalStorageConfig(DatarouterPropertiesLocalStorageConfig.class);
			setJsonSerializer(DefaultHandlerSerializer.class);
		}

		public T setSecretHandlerPermissions(
				Class<? extends SecretHandlerPermissions> secretHandlerPermissions){
			this.secretHandlerPermissions = secretHandlerPermissions;
			return getSelf();
		}

		protected DatarouterSecretWebPlugin getWebPlugin(){
			return new DatarouterSecretWebPlugin(
					buildBasePlugin(),
					secretHandlerPermissions,
					new DatarouterSecretDaoModule(defaultClientId));
		}

		@Override
		public BaseWebPlugin build(){
			return getWebPlugin();
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
