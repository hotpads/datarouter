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

import io.datarouter.secret.config.DatarouterSecretPlugin;
import io.datarouter.secret.config.DatarouterSecretPlugin.DatarouterSecretPluginBuilder;
import io.datarouter.secretweb.service.DatarouterPropertiesAndServiceSecretNamespacer;
import io.datarouter.secretweb.service.DatarouterPropertiesLocalStorageConfig;
import io.datarouter.secretweb.service.DefaultHandlerSerializer;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.DatarouterNavBarCategory;

public class DatarouterSecretWebPlugin extends BaseWebPlugin{

	private final DatarouterSecretPlugin basePlugin;

	private DatarouterSecretWebPlugin(DatarouterSecretPlugin basePlugin){
		this.basePlugin = basePlugin;
		addRouteSet(DatarouterSecretRouteSet.class);
		addDatarouterNavBarItem(
				DatarouterNavBarCategory.CONFIGURATION,
				new DatarouterSecretPaths().datarouter.secrets,
				"Secrets");
		addDatarouterGithubDocLink("datarouter-secrets-web");
	}

	@Override
	public void configure(){
		install(basePlugin);
	}

	public abstract static class DatarouterSecretWebPluginBuilder<T extends DatarouterSecretWebPluginBuilder<T>>
	extends DatarouterSecretPluginBuilder<T>{

		public static class DatarouterSecretWebPluginBuilderImpl
		extends DatarouterSecretWebPluginBuilder<DatarouterSecretWebPluginBuilderImpl>{

			public DatarouterSecretWebPluginBuilderImpl(){
			}

			@Override
			protected DatarouterSecretWebPluginBuilderImpl getSelf(){
				return this;
			}

		}

		public DatarouterSecretWebPluginBuilder(){
			setSecretNamespacer(DatarouterPropertiesAndServiceSecretNamespacer.class);
			setLocalStorageConfig(DatarouterPropertiesLocalStorageConfig.class);
			setJsonSerializer(DefaultHandlerSerializer.class);
		}

		protected DatarouterSecretWebPlugin getWebPlugin(){
			return new DatarouterSecretWebPlugin(
					buildBasePlugin());
		}

		@Override
		public BaseWebPlugin build(){
			return getWebPlugin();
		}

	}

}
