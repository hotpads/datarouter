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
package io.datarouter.secret.service;

import java.util.List;
import java.util.Optional;

import io.datarouter.secret.client.Secret;
import io.datarouter.secret.config.SecretClientSupplierConfig;
import io.datarouter.secret.config.SecretClientSupplierConfigHolder;
import io.datarouter.secret.op.SecretOpConfig;
import io.datarouter.secret.op.SecretOpFactory;
import io.datarouter.secret.op.SecretOpReason;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * This is the recommended interface for accessing {@link Secret}s that need to be written to. Namespacing is
 * automatically applied before and after all interactions with secret names. Except for when a caller switches from one
 * environmentType or service name to another, there is no need to update or migrate secret names.
 */
@Singleton
public class SecretService{

	@Inject
	private SecretOpFactory secretOpFactory;
	@Inject
	private SecretNamespacer secretNamespacer;
	@Inject
	private SecretClientSupplierConfigHolder secretClientSupplierConfigHolder;

	public List<SecretClientSupplierConfig> getSecretClientSupplierConfigs(){
		return secretClientSupplierConfigHolder.getConfigs(secretNamespacer.isDevelopment());
	}

	public <T> void create(String secretName, T secretValue, SecretOpReason reason){
		create(secretName, secretValue, SecretOpConfig.getDefault(reason));
	}

	public <T> void create(String secretName, T secretValue, SecretOpConfig secretOpConfig){
		secretOpFactory.buildCreateOp(secretName, secretValue, secretOpConfig).execute();
	}

	public <T> T read(String secretName, Class<T> secretClass, SecretOpReason reason){
		return read(secretName, secretClass, SecretOpConfig.getDefault(reason));
	}

	public <T> T read(String secretName, Class<T> secretClass, SecretOpConfig secretOpConfig){
		return secretOpFactory.buildReadOp(secretName, secretClass, secretOpConfig).getOutput();
	}

	public <T> void update(String secretName, T secretValue, SecretOpReason reason){
		update(secretName, secretValue, SecretOpConfig.getDefault(reason));
	}

	public <T> void update(String secretName, T secretValue, SecretOpConfig secretOpConfig){
		secretOpFactory.buildUpdateOp(secretName, secretValue, secretOpConfig).execute();
	}

	public void delete(String secretName, SecretOpReason reason){
		delete(secretName, SecretOpConfig.getDefault(reason));
	}

	public void delete(String secretName, SecretOpConfig secretOpConfig){
		secretOpFactory.buildDeleteOp(secretName, secretOpConfig).execute();
	}

	public <T> void put(String secretName, T secretValue, SecretOpReason reason){
		put(secretName, secretValue, SecretOpConfig.getDefault(reason));
	}

	public <T> void put(String secretName, T secretValue, SecretOpConfig secretOpConfig){
		secretOpFactory.buildPutOp(secretName, secretValue, secretOpConfig).execute();
	}

	public List<String> listSecretNames(Optional<String> prefix, SecretOpReason reason){
		return listSecretNames(prefix, SecretOpConfig.getDefault(reason));
	}

	public List<String> listSecretNames(Optional<String> prefix, SecretOpConfig secretOpConfig){
		return secretOpFactory.buildListOp(prefix, secretOpConfig).getOutput();
	}

}
