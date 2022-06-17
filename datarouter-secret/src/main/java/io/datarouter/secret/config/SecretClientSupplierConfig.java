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

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.scanner.Scanner;
import io.datarouter.secret.client.SecretClient.SecretClientSupplier;
import io.datarouter.secret.op.SecretOp;
import io.datarouter.secret.op.client.SecretClientOpType;
import io.datarouter.util.Require;
import io.datarouter.util.string.StringTool;

public class SecretClientSupplierConfig{
	private static final Logger logger = LoggerFactory.getLogger(SecretClientSupplierConfig.class);

	private static final Set<SecretClientOpType> ALL = Set.of(SecretClientOpType.values());
	private static final Set<SecretClientOpType> READ_ONLY = Set.of(SecretClientOpType.LIST, SecretClientOpType.READ);
	private static final Set<SecretClientOpType> READ_ONLY_NO_LIST = Set.of(SecretClientOpType.READ);

	private final String configName;
	private final Class<? extends SecretClientSupplier> secretClientSupplierClass;
	private final Set<SecretClientOpType> allowedOps;
	private final Optional<Set<String>> allowedNames;

	private SecretClientSupplierConfig(String configName,
			Class<? extends SecretClientSupplier> secretClientSupplierClass,
			Set<SecretClientOpType> allowedOps, Set<String> allowedNames){
		Require.isTrue(StringTool.notNullNorEmptyNorWhitespace(configName));
		Require.notNull(secretClientSupplierClass);
		this.configName = configName;
		this.secretClientSupplierClass = secretClientSupplierClass;
		this.allowedOps = allowedOps;
		this.allowedNames = allowedNames.size() > 0 ? Optional.of(allowedNames) : Optional.empty();
	}

	public static SecretClientSupplierConfig allOps(String configName,
			Class<? extends SecretClientSupplier> secretClientClass){
		return new SecretClientSupplierConfig(configName, secretClientClass, ALL, Set.of());
	}

	public static SecretClientSupplierConfig readOnly(String configName,
			Class<? extends SecretClientSupplier> secretClientClass){
		return new SecretClientSupplierConfig(configName, secretClientClass, READ_ONLY, Set.of());
	}

	public static SecretClientSupplierConfig readOnlyWithNames(String configName,
			Class<? extends SecretClientSupplier> secretClientClass, Set<String> secretNames){
		Require.notEmpty(secretNames);
		//do not allow list, since it might reveal names other than the specified ones
		return new SecretClientSupplierConfig(configName, secretClientClass, READ_ONLY_NO_LIST, secretNames);
	}

	public Class<? extends SecretClientSupplier> getSecretClientSupplierClass(){
		return secretClientSupplierClass;
	}

	public String getConfigName(){
		return configName;
	}

	public Set<SecretClientOpType> getAllowedOps(){
		return allowedOps;
	}

	public Optional<Set<String>> getAllowedNames(){
		return allowedNames;
	}

	public boolean allowed(SecretOp<?,?,?,?> secretOp){
		if(secretOp.getTargetSecretClientConfig().isPresent() && !secretOp.getTargetSecretClientConfig().get()
				.equals(configName)){
			logNotAllowed(secretOp, "Config name not allowed.");
			return false;
		}
		if(!allowedOps.contains(secretOp.getSecretClientOpType())){
			logNotAllowed(secretOp, "Op not allowed.");
			return false;
		}
		//TODO allow regex or wildcard match in future?
		if(!allowedNames.map(names -> names.contains(secretOp.getName())).orElse(true)){
			logNotAllowed(secretOp, "Secret name not allowed.");
			return false;
		}
		return true;
	}

	private void logNotAllowed(SecretOp<?,?,?,?> secretOp, String reason){
		String allowedOpsString = Scanner.of(allowedOps)
				.map(op -> op.persistentString)
				.collect(Collectors.joining(",", "[", "]"));

		String allowedNamesString = allowedNames.isPresent()
				? Scanner.of(allowedNames.get()).collect(Collectors.joining(",", "[", "]"))
				: "<ALL>";
		logger.warn("Skipping configName={} for secretOpInfo={}. {} allowedOps={}, allowedNames={}, "
				+ "secretClientSupplierClass={}", configName, secretOp, reason, allowedOpsString, allowedNamesString,
				secretClientSupplierClass.getSimpleName());
	}

}