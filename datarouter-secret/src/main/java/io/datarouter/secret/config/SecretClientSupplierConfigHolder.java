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

import io.datarouter.scanner.Scanner;
import io.datarouter.secret.op.SecretOp;

public class SecretClientSupplierConfigHolder{

	//TODO prevent multiple writers and put writer before readers in order

	private final List<SecretClientSupplierConfig> developmentConfigs;
	private final List<SecretClientSupplierConfig> configs;

	//use if no clients are desired
	public SecretClientSupplierConfigHolder(){
		this(List.of());
	}

	public SecretClientSupplierConfigHolder(List<SecretClientSupplierConfig> configs){
		this(configs, configs);
	}

	public SecretClientSupplierConfigHolder(List<SecretClientSupplierConfig> developmentConfigs,
			List<SecretClientSupplierConfig> configs){
		this.developmentConfigs = List.copyOf(developmentConfigs);
		this.configs = List.copyOf(configs);
	}

	public Scanner<SecretClientSupplierConfig> getAllowedConfigs(boolean isDevelopment, SecretOp<?,?,?,?> secretOp){
		return Scanner.of(isDevelopment ? developmentConfigs : configs)
				.include(secretClientSupplierConfig -> secretClientSupplierConfig.allowed(secretOp));
	}

	public List<SecretClientSupplierConfig> getConfigs(boolean isDevelopment){
		return isDevelopment ? developmentConfigs : configs;
	}

}