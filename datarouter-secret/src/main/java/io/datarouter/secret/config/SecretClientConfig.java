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

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.scanner.Scanner;
import io.datarouter.secret.client.SecretClientSupplier;
import io.datarouter.secret.op.SecretOp;
import io.datarouter.secret.op.SecretOpInfo;
import io.datarouter.util.Require;

public class SecretClientConfig{
	private static final Logger logger = LoggerFactory.getLogger(SecretClientConfig.class);

	private static final Set<SecretOp> ALL = Set.of(SecretOp.values());
	private static final Set<SecretOp> READ_ONLY = Set.of(SecretOp.LIST, SecretOp.READ);
	private static final Set<SecretOp> READ_ONLY_NO_LIST = Set.of(SecretOp.READ);

	private final Class<? extends SecretClientSupplier> secretClientSupplierClass;
	private final Set<SecretOp> allowedOps;
	private final Optional<Set<String>> allowedNames;

	private SecretClientConfig(Class<? extends SecretClientSupplier> secretClientSupplierClass,
			Set<SecretOp> allowedOps, Set<String> allowedNames){
		Require.notNull(secretClientSupplierClass);
		this.secretClientSupplierClass = secretClientSupplierClass;
		this.allowedOps = allowedOps;
		this.allowedNames = allowedNames.size() > 0 ? Optional.of(allowedNames) : Optional.empty();
	}

	public static SecretClientConfig allOps(Class<? extends SecretClientSupplier> secretClientClass){
		return new SecretClientConfig(secretClientClass, ALL, Set.of());
	}

	public static SecretClientConfig readOnly(Class<? extends SecretClientSupplier> secretClientClass){
		return new SecretClientConfig(secretClientClass, READ_ONLY, Set.of());
	}

	public static SecretClientConfig readOnlyWithNames(
			Class<? extends SecretClientSupplier> secretClientClass, Set<String> secretNames){
		Require.notEmpty(secretNames);
		//do not allow list, since it might reveal names other than the specified ones
		return new SecretClientConfig(secretClientClass, READ_ONLY_NO_LIST, secretNames);
	}

	public Class<? extends SecretClientSupplier> getSecretClientSupplierClass(){
		return secretClientSupplierClass;
	}

	public boolean allowed(SecretOpInfo opInfo){
		if(!allowedOps.contains(opInfo.op)){
			logNotAllowed(opInfo, "Op not allowed.");
			return false;
		}
		//TODO allow regex or wildcard match in future?
		if(!allowedNames.map(names -> names.contains(opInfo.name)).orElse(true)){
			logNotAllowed(opInfo, "Name not allowed.");
			return false;
		}
		return true;
	}

	private void logNotAllowed(SecretOpInfo opInfo, String reason){
		String allowedOpsString = Scanner.of(allowedOps)
				.map(SecretOp::getPersistentString)
				.collect(Collectors.joining(",", "[", "]"));

		String allowedNamesString = allowedNames.isPresent()
				? Scanner.of(allowedNames.get()).collect(Collectors.joining(",", "[", "]"))
				: "<ALL>";
		logger.warn("Skipping secretClientSupplierClass={} for secretOpInfo={}. {} allowedOps={}, allowedNames={}",
				secretClientSupplierClass.getSimpleName(), opInfo, reason, allowedOpsString, allowedNamesString);
	}

}