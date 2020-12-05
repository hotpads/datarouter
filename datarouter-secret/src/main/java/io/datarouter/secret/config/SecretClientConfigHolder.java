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

import java.util.List;

import io.datarouter.scanner.Scanner;
import io.datarouter.secret.client.SecretClientSupplier;
import io.datarouter.secret.op.SecretOpInfo;

public class SecretClientConfigHolder{

	private final List<SecretClientConfig> developmentConfigs;
	private final List<SecretClientConfig> configs;

	//use if no clients are desired
	public SecretClientConfigHolder(){
		this(List.of());
	}

	public SecretClientConfigHolder(List<SecretClientConfig> configs){
		this(configs, configs);
	}

	public SecretClientConfigHolder(List<SecretClientConfig> developmentConfigs, List<SecretClientConfig> configs){
		this.developmentConfigs = List.copyOf(developmentConfigs);
		this.configs = List.copyOf(configs);
	}

	public Scanner<Class<? extends SecretClientSupplier>> getAllowedSecretClientSupplierClasses(boolean isDevelopment,
			SecretOpInfo secretOpInfo){
		return Scanner.of(isDevelopment ? developmentConfigs : configs)
				.include(secretClientSupplierConfig -> secretClientSupplierConfig.allowed(secretOpInfo))
				.map(SecretClientConfig::getSecretClientSupplierClass);
	}

}