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
package io.datarouter.secret.config;

import java.util.List;

import io.datarouter.httpclient.endpoint.DatarouterSharedSecretRetriever;
import io.datarouter.inject.guice.BaseGuiceModule;
import io.datarouter.inject.guice.BasePlugin;
import io.datarouter.secret.client.local.LocalStorageConfig;
import io.datarouter.secret.client.local.LocalStorageConfig.DefaultLocalStorageConfig;
import io.datarouter.secret.client.local.LocalStorageSecretClientSupplier;
import io.datarouter.secret.client.memory.InjectedDefaultMemorySecretClientSupplier.DefaultMemorySecrets;
import io.datarouter.secret.service.CachedSecretFactory;
import io.datarouter.secret.service.SecretJsonSerializer;
import io.datarouter.secret.service.SecretJsonSerializer.GsonToolJsonSerializer;
import io.datarouter.secret.service.SecretNamespacer;
import io.datarouter.secret.service.SecretNamespacer.DevelopmentNamespacer;

public class DatarouterSecretPlugin extends BasePlugin{

	private final SecretClientSupplierConfigHolder secretClientConfigHolder;
	private final Class<? extends SecretNamespacer> secretNamespacer;
	private final Class<? extends SecretJsonSerializer> jsonSerializer;
	private final Class<? extends LocalStorageConfig> localStorageConfig;
	private final DefaultMemorySecrets defaultMemorySecrets;

	private DatarouterSecretPlugin(
			SecretClientSupplierConfigHolder secretClientConfigHolder,
			Class<? extends SecretNamespacer> secretNamespacer,
			Class<? extends SecretJsonSerializer> jsonSerializer,
			Class<? extends LocalStorageConfig> localStorageConfig,
			DefaultMemorySecrets defaultMemorySecrets){
		this.secretClientConfigHolder = secretClientConfigHolder;
		this.secretNamespacer = secretNamespacer;
		this.jsonSerializer = jsonSerializer;
		this.localStorageConfig = localStorageConfig;
		this.defaultMemorySecrets = defaultMemorySecrets;
	}

	@Override
	public void configure(){
		bindActualInstance(SecretClientSupplierConfigHolder.class, secretClientConfigHolder);
		bindActual(SecretNamespacer.class, secretNamespacer);
		bindActual(SecretJsonSerializer.class, jsonSerializer);
		bindActual(LocalStorageConfig.class, localStorageConfig);
		bindActualInstance(DefaultMemorySecrets.class, defaultMemorySecrets);
		bindActual(DatarouterSharedSecretRetriever.class, CachedSecretFactory.class);
	}

	@Override
	public BaseGuiceModule getAsDefaultBinderModule(){
		return new DatarouterSecretPluginDefaults();
	}

	public class DatarouterSecretPluginDefaults extends BaseGuiceModule{

		@Override
		public void configure(){
			bindDefaultInstance(SecretClientSupplierConfigHolder.class, secretClientConfigHolder);
			bindDefault(SecretNamespacer.class, secretNamespacer);
			bindDefault(SecretJsonSerializer.class, jsonSerializer);
			bindDefault(LocalStorageConfig.class, localStorageConfig);
			bindDefaultInstance(DefaultMemorySecrets.class, defaultMemorySecrets);
		}

	}

	public abstract static class DatarouterSecretPluginBuilder<T extends DatarouterSecretPluginBuilder<T>>{

		public static final SecretClientSupplierConfig LOCAL_STORAGE_ALL_OPS = SecretClientSupplierConfig.allOps(
				"LOCAL_STORAGE_ALL_OPS",
				LocalStorageSecretClientSupplier.class);

		private SecretClientSupplierConfigHolder secretClientConfigHolder = new SecretClientSupplierConfigHolder(
				List.of(LOCAL_STORAGE_ALL_OPS));
		private Class<? extends SecretNamespacer> secretNamespacer = DevelopmentNamespacer.class;
		private Class<? extends SecretJsonSerializer> jsonSerializer = GsonToolJsonSerializer.class;
		private Class<? extends LocalStorageConfig> localStorageConfig = DefaultLocalStorageConfig.class;
		private DefaultMemorySecrets defaultMemorySecrets = new DefaultMemorySecrets();

		public static class DatarouterSecretPluginBuilderImpl
		extends DatarouterSecretPluginBuilder<DatarouterSecretPluginBuilderImpl>{

			@Override
			protected DatarouterSecretPluginBuilderImpl getSelf(){
				return this;
			}

		}

		protected abstract T getSelf();

		public T setSecretClientSupplierConfigHolder(SecretClientSupplierConfigHolder secretClientConfigHolder){
			this.secretClientConfigHolder = secretClientConfigHolder;
			return getSelf();
		}

		public T setSecretNamespacer(Class<? extends SecretNamespacer> secretNamespacer){
			this.secretNamespacer = secretNamespacer;
			return getSelf();
		}

		public T setJsonSerializer(Class<? extends SecretJsonSerializer> jsonSerializer){
			this.jsonSerializer = jsonSerializer;
			return getSelf();
		}

		public T setLocalStorageConfig(Class<? extends LocalStorageConfig> localStorageConfig){
			this.localStorageConfig = localStorageConfig;
			return getSelf();
		}

		public T setDefaultMemorySecrets(DefaultMemorySecrets defaultMemorySecrets){
			this.defaultMemorySecrets = defaultMemorySecrets;
			return getSelf();
		}

		protected DatarouterSecretPlugin buildBasePlugin(){
			return new DatarouterSecretPlugin(
					secretClientConfigHolder,
					secretNamespacer,
					jsonSerializer,
					localStorageConfig,
					defaultMemorySecrets);
		}

		public BasePlugin build(){
			return buildBasePlugin();
		}

	}

}
