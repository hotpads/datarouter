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
package io.datarouter.secret.client.memory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.scanner.Scanner;
import io.datarouter.secret.client.SecretClient;
import io.datarouter.secret.client.SecretClient.SecretClientSupplier;
import io.datarouter.secret.service.SecretJsonSerializer;
import io.datarouter.secret.service.SecretNamespacer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class InjectedDefaultMemorySecretClientSupplier implements SecretClientSupplier{
	private static final Logger logger = LoggerFactory.getLogger(InjectedDefaultMemorySecretClientSupplier.class);

	@Inject
	private SecretJsonSerializer secretJsonSerializer;
	@Inject
	private SecretNamespacer secretNamespacer;
	@Inject
	private DefaultMemorySecrets initialValues;

	private MemorySecretClient client;

	@Override
	public SecretClient get(){
		if(client == null){
			synchronized(this){
				if(client == null){
					Map<String,String> combinedSecrets = new HashMap<>();
					addSecrets(combinedSecrets, initialValues.appSecrets, secretNamespacer::appNamespaced);
					addSecrets(combinedSecrets, initialValues.sharedSecrets, secretNamespacer::sharedNamespaced);
					addSecrets(combinedSecrets, initialValues.preNamespacedSecrets, UnaryOperator.identity());
					client = new MemorySecretClient(combinedSecrets);
				}
			}
		}
		return client;
	}

	private void addSecrets(Map<String,String> combined, Map<String,Object> secrets, UnaryOperator<String> namespacer){
		secrets.forEach((name, value) -> {
			name = namespacer.apply(name);
			if(combined.put(name, secretJsonSerializer.serialize(value)) != null){
				logger.warn("more than one put for namespaced and combined memory secret name={}", name);
			}
		});
	}

	public static final class DefaultMemorySecrets{
		private static final Logger logger = LoggerFactory.getLogger(DefaultMemorySecrets.class);

		private final Map<String,Object> appSecrets;
		private final Map<String,Object> sharedSecrets;
		private final Map<String,Object> preNamespacedSecrets;

		public DefaultMemorySecrets(){
			this.appSecrets = new HashMap<>();
			this.sharedSecrets = new HashMap<>();
			this.preNamespacedSecrets = new HashMap<>();
		}

		public DefaultMemorySecrets(DefaultMemorySecrets other){
			this.appSecrets = new HashMap<>(other.appSecrets);
			this.sharedSecrets = new HashMap<>(other.sharedSecrets);
			this.preNamespacedSecrets = new HashMap<>(other.preNamespacedSecrets);
		}

		public DefaultMemorySecrets addAppSecret(SecretDefaultDto dto){
			return putSecret(appSecrets, dto.name(), dto.defaultValue());
		}

		public DefaultMemorySecrets addSharedSecret(SecretDefaultDto dto){
			return putSecret(sharedSecrets, dto.name(), dto.defaultValue());
		}

		public DefaultMemorySecrets addSharedSecrets(List<SecretDefaultDto> dtos){
			Scanner.of(dtos).forEach(this::addSharedSecret);
			return this;
		}

		private DefaultMemorySecrets putSecret(Map<String,Object> map, String name, Object value){
			if(map.put(name, value) != null){
				logger.warn("more than one put for default secret name={}", name);
			}
			return this;
		}

		public record SecretDefaultDto(
				String name,
				Object defaultValue){}

	}

}
