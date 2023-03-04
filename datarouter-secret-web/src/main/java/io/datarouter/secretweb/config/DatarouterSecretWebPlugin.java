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
package io.datarouter.secretweb.config;

import java.util.List;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.secret.config.DatarouterSecretPlugin;
import io.datarouter.secret.config.DatarouterSecretPlugin.DatarouterSecretPluginBuilder;
import io.datarouter.secretweb.service.DatarouterPropertiesAndServiceSecretNamespacer;
import io.datarouter.secretweb.service.DatarouterPropertiesLocalStorageConfig;
import io.datarouter.secretweb.service.DefaultHandlerSerializer;
import io.datarouter.secretweb.storage.oprecord.DatarouterSecretOpRecordDao;
import io.datarouter.secretweb.storage.oprecord.DatarouterSecretOpRecordDao.DaoSecretOpRecorderSupplier;
import io.datarouter.secretweb.storage.oprecord.DatarouterSecretOpRecordDao.DatarouterSecretOpRecordDaoParams;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterSecretWebPlugin extends BaseWebPlugin{

	private final DatarouterSecretPlugin basePlugin;

	private DatarouterSecretWebPlugin(DatarouterSecretPlugin basePlugin,
			DatarouterSecretDaoModule daosModuleBuilder){
		this.basePlugin = basePlugin;
		addRouteSet(DatarouterSecretRouteSet.class);
		setDaosModule(daosModuleBuilder);
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.KEYS,
				new DatarouterSecretPaths().datarouter.secrets,
				"Secrets");
		addDatarouterGithubDocLink("datarouter-secrets-web");
		addSettingRoot(DatarouterSecretWebSettingRoot.class);
		addPluginEntry(BaseTriggerGroup.KEY, DatarouterSecretWebTriggerGroup.class);
	}

	@Override
	public void configure(){
		install(basePlugin);
	}

	public abstract static class DatarouterSecretWebPluginBuilder<T extends DatarouterSecretWebPluginBuilder<T>>
	extends DatarouterSecretPluginBuilder<T>{

		private final List<ClientId> defaultClientId;

		public static class DatarouterSecretWebPluginBuilderImpl
		extends DatarouterSecretWebPluginBuilder<DatarouterSecretWebPluginBuilderImpl>{

			public DatarouterSecretWebPluginBuilderImpl(List<ClientId> defaultClientId){
				super(defaultClientId);
			}

			@Override
			protected DatarouterSecretWebPluginBuilderImpl getSelf(){
				return this;
			}

		}

		public DatarouterSecretWebPluginBuilder(List<ClientId> defaultClientId){
			this.defaultClientId = defaultClientId;
			setSecretNamespacer(DatarouterPropertiesAndServiceSecretNamespacer.class);
			setSecretOpRecorderSupplier(DaoSecretOpRecorderSupplier.class);
			setLocalStorageConfig(DatarouterPropertiesLocalStorageConfig.class);
			setJsonSerializer(DefaultHandlerSerializer.class);
		}

		protected DatarouterSecretWebPlugin getWebPlugin(){
			return new DatarouterSecretWebPlugin(
					buildBasePlugin(),
					new DatarouterSecretDaoModule(defaultClientId));
		}

		@Override
		public BaseWebPlugin build(){
			return getWebPlugin();
		}

	}

	public static class DatarouterSecretDaoModule extends DaosModuleBuilder{

		private final List<ClientId> datarouterSecretOpRecordDaoClientId;

		public DatarouterSecretDaoModule(List<ClientId> datarouterSecretOpRecordDaoClientId){
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
