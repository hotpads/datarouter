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
package io.datarouter.secret.client.local;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.secret.client.BaseSecretClient;
import io.datarouter.secret.client.Secret;
import io.datarouter.secret.client.SecretClient;
import io.datarouter.secret.client.SecretClientSupplier;
import io.datarouter.util.properties.PropertiesTool;

/**
 * This is an implementation of {@link SecretClient} that stores all {@link Secret}s in a local plaintext properties
 * file. It is only suitable for local development work, since all storage is plaintext.
 */
@Singleton
public class LocalStorageSecretClient extends BaseSecretClient{

	@Inject
	private LocalStorageConfig config;
	@Inject
	private LocalStorageDefaultSecretValues localStorageDefaultSecretValues;

	private Properties readSecrets(){
		try{
			return PropertiesTool.parse(config.getConfigFilePath());
		}catch(RuntimeException e){
			File file = new File(config.getConfigFilePath());
			try{
				file.createNewFile();
			}catch(IOException e1){
				throw new RuntimeException("failed to create properties file");
			}
			return PropertiesTool.parse(config.getConfigFilePath());
		}

	}

	private void writeSecrets(Properties properties){
		PropertiesTool.writeToFile(properties, config.getConfigFilePath());
	}

	@Override
	public synchronized void createInternal(Secret secret){
		Properties secrets = readSecrets();
		if(secrets.containsKey(secret.getName())){
			throw new RuntimeException("secret exists name=" + secret.getName());
		}
		secrets.put(secret.getName(), secret.getValue());
		writeSecrets(secrets);
	}

	@Override
	public synchronized Secret readInternal(String name){
		Properties secrets = readSecrets();
		if(!secrets.containsKey(name)){
			return localStorageDefaultSecretValues.readDefaultValue(name)
					.orElseThrow(() -> new RuntimeException("secret does not exist name=" + name));
		}
		return new Secret(name, secrets.getProperty(name));
	}

	@Override
	public synchronized List<String> listInternal(Optional<String> exclusivePrefix){
		Properties secrets = readSecrets();
		return secrets.keySet().stream()
				.map(obj -> (String)obj)
				.filter(name -> {
					return exclusivePrefix
							.map(prefix -> prefix.length() < name.length() && name.startsWith(prefix))
							.orElse(true);
				})
				.collect(Collectors.toList());
	}

	@Override
	public synchronized void updateInternal(Secret secret){
		Properties secrets = readSecrets();
		if(!secrets.containsKey(secret.getName())){
			throw new RuntimeException("secret does not exist name=" + secret.getName());
		}
		secrets.put(secret.getName(), secret.getValue());
		writeSecrets(secrets);
	}

	@Override
	public synchronized void deleteInternal(String name){
		Properties secrets = readSecrets();
		if(!secrets.containsKey(name)){
			throw new RuntimeException("secret does not exist name=" + name);
		}
		secrets.remove(name);
		writeSecrets(secrets);
	}

	@Singleton
	public static class LocalStorageSecretClientSupplier implements SecretClientSupplier{

		@Inject
		private LocalStorageSecretClient localStorageSecretClient;

		@Override
		public SecretClient get(){
			return localStorageSecretClient;
		}

	}

	@Singleton
	public static final class LocalStorageDefaultSecretValues{

		private final ConcurrentHashMap<String,String> defaultSecretValues;

		public LocalStorageDefaultSecretValues(Map<String,String> initialLocalStorageSecretValues){
			defaultSecretValues = new ConcurrentHashMap<>(initialLocalStorageSecretValues);
		}

		private Optional<Secret> readDefaultValue(String name){
			return Optional.ofNullable(defaultSecretValues.get(name))
					.map(value -> new Secret(name, value));
		}

		public void registerDefaultValue(String name, String value){
			defaultSecretValues.compute(name, (key, oldValue) -> {
				if(oldValue == null){
					return value;
				}
				if(!value.equals(oldValue)){
					throw new RuntimeException("Multiple conflicting default values for secret name=" + name);
				}
				return oldValue;
			});
		}

	}

}
