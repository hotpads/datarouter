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
package io.datarouter.secret.client.local;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import io.datarouter.secret.client.Secret;
import io.datarouter.secret.client.SecretClient;
import io.datarouter.secret.exception.SecretExistsException;
import io.datarouter.secret.exception.SecretNotFoundException;
import io.datarouter.util.properties.PropertiesTool;

/**
 * This is an implementation of {@link SecretClient} that stores all {@link Secret}s in a local plaintext properties
 * file. It is only suitable for local development work, since all storage is plaintext.
 */
public class LocalStorageSecretClient implements SecretClient{

	private final LocalStorageConfig config;

	public LocalStorageSecretClient(LocalStorageConfig config){
		this.config = config;
	}

	private Properties readSecrets(){
		try{
			return PropertiesTool.parse(config.getConfigFilePath());
		}catch(RuntimeException e1){
			File file = new File(config.getConfigFilePath());
			try{
				file.createNewFile();
			}catch(IOException e2){
				throw new RuntimeException("failed to create properties file");
			}
			return PropertiesTool.parse(config.getConfigFilePath());
		}

	}

	private void writeSecrets(Properties properties){
		PropertiesTool.writeToFile(properties, config.getConfigFilePath());
	}

	@Override
	public final synchronized void create(Secret secret){
		Properties secrets = readSecrets();
		if(secrets.containsKey(secret.getName())){
			throw new SecretExistsException(secret.getName());
		}
		secrets.put(secret.getName(), secret.getValue());
		writeSecrets(secrets);
	}

	@Override
	public final synchronized Secret read(String name){
		Properties secrets = readSecrets();
		if(!secrets.containsKey(name)){
			throw new SecretNotFoundException(name);
		}
		return new Secret(name, secrets.getProperty(name));
	}

	@Override
	public final synchronized List<String> listNames(Optional<String> prefix){
		Properties secrets = readSecrets();
		return secrets.keySet().stream()
				.map(obj -> (String)obj)
				.filter(name -> prefix.map(current -> current.length() < name.length() && name.startsWith(current))
						.orElse(true))
				.toList();
	}

	@Override
	public final synchronized void update(Secret secret){
		Properties secrets = readSecrets();
		if(!secrets.containsKey(secret.getName())){
			throw new SecretNotFoundException(secret.getName());
		}
		secrets.put(secret.getName(), secret.getValue());
		writeSecrets(secrets);
	}

	@Override
	public final synchronized void delete(String name){
		Properties secrets = readSecrets();
		if(!secrets.containsKey(name)){
			throw new SecretNotFoundException(name);
		}
		secrets.remove(name);
		writeSecrets(secrets);
	}

}
