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
import java.util.Map;
import java.util.stream.Collectors;

import io.datarouter.inject.guice.BaseGuiceModule;
import io.datarouter.inject.guice.BasePlugin;
import io.datarouter.secret.client.Secret;
import io.datarouter.secret.client.SecretClientSupplier;
import io.datarouter.secret.client.local.LocalStorageConfig;
import io.datarouter.secret.client.local.LocalStorageConfig.DefaultLocalStorageConfig;
import io.datarouter.secret.client.local.LocalStorageSecretClient.LocalStorageDefaultSecretValues;
import io.datarouter.secret.client.local.LocalStorageSecretClient.LocalStorageSecretClientSupplier;
import io.datarouter.secret.service.SecretJsonSerializer;
import io.datarouter.secret.service.SecretJsonSerializer.GsonToolJsonSerializer;
import io.datarouter.secret.service.SecretNamespacer;
import io.datarouter.secret.service.SecretNamespacer.EmptyNamespacer;
import io.datarouter.secret.service.SecretOpRecorderSupplier;
import io.datarouter.secret.service.SecretOpRecorderSupplier.NoOpSecretOpRecorderSupplier;
import io.datarouter.secret.service.SecretStageDetector;
import io.datarouter.secret.service.SecretStageDetector.DevelopmentSecretStageDetector;

public class DatarouterSecretPlugin extends BasePlugin{

	private final Class<? extends SecretClientSupplier> secretClientSupplier;
	private final Class<? extends SecretNamespacer> secretNamespacer;
	private final Class<? extends SecretOpRecorderSupplier> secretOpRecorderSupplier;
	private final Class<? extends SecretJsonSerializer> jsonSerializer;
	private final Class<? extends SecretStageDetector> secretStageDetector;
	private final Class<? extends LocalStorageConfig> localStorageConfig;
	private final Map<String,String> initialLocalStorageSecretValues;

	private DatarouterSecretPlugin(
			Class<? extends SecretClientSupplier> secretClientSupplier,
			Class<? extends SecretNamespacer> secretNamespacer,
			Class<? extends SecretOpRecorderSupplier> secretOpRecorderSupplier,
			Class<? extends SecretJsonSerializer> jsonSerializer,
			Class<? extends SecretStageDetector> secretStageDetector,
			Class<? extends LocalStorageConfig> localStorageConfig,
			Map<String,String> initialLocalStorageSecretValues){
		this.secretClientSupplier = secretClientSupplier;
		this.secretNamespacer = secretNamespacer;
		this.secretOpRecorderSupplier = secretOpRecorderSupplier;
		this.jsonSerializer = jsonSerializer;
		this.secretStageDetector = secretStageDetector;
		this.localStorageConfig = localStorageConfig;
		this.initialLocalStorageSecretValues = initialLocalStorageSecretValues;
	}

	@Override
	public String getName(){
		return "DatarouterSecret";
	}

	@Override
	public void configure(){
		bindActual(SecretClientSupplier.class, secretClientSupplier);
		bindActual(SecretNamespacer.class, secretNamespacer);
		bindActual(SecretOpRecorderSupplier.class, secretOpRecorderSupplier);
		bindActual(SecretJsonSerializer.class, jsonSerializer);
		bindActual(SecretStageDetector.class, secretStageDetector);
		bindActual(LocalStorageConfig.class, localStorageConfig);
		bindActualInstance(LocalStorageDefaultSecretValues.class, new LocalStorageDefaultSecretValues(
				initialLocalStorageSecretValues));
	}

	@Override
	public BaseGuiceModule getAsDefaultBinderModule(){
		return new DatarouterSecretPluginDefaults();
	}

	public class DatarouterSecretPluginDefaults extends BaseGuiceModule{

		@Override
		public void configure(){
			bindDefault(SecretClientSupplier.class, secretClientSupplier);
			bindDefault(SecretNamespacer.class, secretNamespacer);
			bindDefault(SecretOpRecorderSupplier.class, secretOpRecorderSupplier);
			bindDefault(SecretJsonSerializer.class, jsonSerializer);
			bindDefault(SecretStageDetector.class, secretStageDetector);
			bindDefault(LocalStorageConfig.class, localStorageConfig);
			bindDefaultInstance(LocalStorageDefaultSecretValues.class, new LocalStorageDefaultSecretValues(
					initialLocalStorageSecretValues));
		}

	}

	public abstract static class DatarouterSecretPluginBuilder<T extends DatarouterSecretPluginBuilder<T>>{

		private Map<String,String> initialLocalStorageSecretValues = new HashMap<>();
		private Class<? extends SecretNamespacer> secretNamespacer = EmptyNamespacer.class;
		private Class<? extends SecretOpRecorderSupplier> secretOpRecorderSupplier = NoOpSecretOpRecorderSupplier.class;
		private Class<? extends SecretJsonSerializer> jsonSerializer = GsonToolJsonSerializer.class;
		private Class<? extends SecretStageDetector> secretStageDetector = DevelopmentSecretStageDetector.class;
		private Class<? extends LocalStorageConfig> localStorageConfig = DefaultLocalStorageConfig.class;
		private Class<? extends SecretClientSupplier> secretClientSupplier = LocalStorageSecretClientSupplier.class;

		public static class DatarouterSecretPluginBuilderImpl
		extends DatarouterSecretPluginBuilder<DatarouterSecretPluginBuilderImpl>{

			@Override
			protected DatarouterSecretPluginBuilderImpl getSelf(){
				return this;
			}

		}

		protected abstract T getSelf();

		public T setSecretClientSupplier(
				Class<? extends SecretClientSupplier> secretClientSupplier){
			this.secretClientSupplier = secretClientSupplier;
			return getSelf();
		}

		public T setSecretNamespacer(Class<? extends SecretNamespacer> secretNamespacer){
			this.secretNamespacer = secretNamespacer;
			return getSelf();
		}

		public T setSecretOpRecorderSupplier(Class<? extends SecretOpRecorderSupplier> secretOpRecorderSupplier){
			this.secretOpRecorderSupplier = secretOpRecorderSupplier;
			return getSelf();
		}

		public T setJsonSerializer(Class<? extends SecretJsonSerializer> jsonSerializer){
			this.jsonSerializer = jsonSerializer;
			return getSelf();
		}

		public T setSecretStageDetector(
				Class<? extends SecretStageDetector> secretStageDetector){
			this.secretStageDetector = secretStageDetector;
			return getSelf();
		}

		public T setLocalStorageConfig(Class<? extends LocalStorageConfig> localStorageConfig){
			this.localStorageConfig = localStorageConfig;
			return getSelf();
		}

		public T setInitialLocalStorageSecrets(Collection<Secret> secrets){
			initialLocalStorageSecretValues = secrets.stream()
					.collect(Collectors.toMap(Secret::getName, Secret::getValue));
			return getSelf();
		}

		public T addInitialLocalStorageSecret(Secret secret){
			initialLocalStorageSecretValues.put(secret.getName(), secret.getValue());
			return getSelf();
		}

		public T addInitialLocalStorageSecrets(Collection<Secret> secrets){
			initialLocalStorageSecretValues.putAll(secrets.stream()
					.collect(Collectors.toMap(Secret::getName, Secret::getValue)));
			return getSelf();
		}

		protected DatarouterSecretPlugin buildBasePlugin(){
			return new DatarouterSecretPlugin(
					secretClientSupplier,
					secretNamespacer,
					secretOpRecorderSupplier,
					jsonSerializer,
					secretStageDetector,
					localStorageConfig,
					initialLocalStorageSecretValues);
		}

		public BasePlugin build(){
			return buildBasePlugin();
		}

	}

}
