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
package io.datarouter.secret.client.memory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import io.datarouter.secret.client.BaseSecretClient;
import io.datarouter.secret.client.Secret;
import io.datarouter.secret.exception.SecretExistsException;
import io.datarouter.secret.exception.SecretNotFoundException;

public class MemorySecretClient extends BaseSecretClient{

	private final ConcurrentMap<String,String> secrets;

	public MemorySecretClient(){
		this.secrets = new ConcurrentHashMap<>();
	}

	public MemorySecretClient(Map<String,String> secrets){
		this.secrets = new ConcurrentHashMap<>(secrets);
	}

	@Override
	protected void createInternal(Secret secret){
		if(secrets.putIfAbsent(secret.getName(), secret.getValue()) != null){
			throw new SecretExistsException(secret.getName());
		}
	}

	@Override
	protected Secret readInternal(String name){
		return Optional.ofNullable(secrets.get(name))
				.map(value -> new Secret(name, value))
				.orElseThrow(() -> new SecretNotFoundException(name));
	}

	@Override
	protected List<String> listInternal(Optional<String> exclusivePrefix){
		Set<String> names;
		synchronized(secrets){
			names = Set.copyOf(secrets.keySet());
		}
		return names.stream()
				.map(obj -> obj)
				.filter(name -> {
					return exclusivePrefix
							.map(prefix -> prefix.length() < name.length() && name.startsWith(prefix))
							.orElse(true);
				})
				.collect(Collectors.toList());
	}

	@Override
	protected void updateInternal(Secret secret){
		if(secrets.computeIfPresent(secret.getName(), (key, value) -> secret.getValue()) == null){
			throw new SecretNotFoundException(secret.getName());
		}
	}

	@Override
	protected void deleteInternal(String name){
		if(secrets.remove(name) == null){
			throw new SecretNotFoundException(name);
		}
	}

}
