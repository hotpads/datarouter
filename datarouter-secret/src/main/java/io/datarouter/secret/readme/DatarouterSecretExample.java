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
package io.datarouter.secret.readme;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.secret.client.SecretClient;
import io.datarouter.secret.client.memory.MemorySecretClientSupplier;
import io.datarouter.secret.op.SecretOpConfig;
import io.datarouter.secret.op.SecretOpReason;
import io.datarouter.secret.service.CachedSecretFactory;
import io.datarouter.secret.service.CachedSecretFactory.CachedSecret;
import io.datarouter.secret.service.SecretService;

@Singleton
public class DatarouterSecretExample{//TODO update this and md next pass

	//formatting doesn't really matter as long as the underlying implementation allows it (SecretClient#validateName)
	private static final String SECRET_NAME = "examples/example-secret";

	@Inject
	private MemorySecretClientSupplier secretClientSupplier;
	@Inject
	private SecretService secretService;
	@Inject
	private CachedSecretFactory cachedSecretFactory;

	//SecretOpReason will be recorded using the configured SecretOpRecorder implementation
	public void copySharedSecretToAppSecret(){
		SecretOpConfig config = SecretOpConfig.builder(SecretOpReason.automatedOp("a job might do this"))
				.useSharedNamespace()
				.build();
		String sharedSecret = secretService.read(SECRET_NAME, String.class, config);
		secretService.put(SECRET_NAME, sharedSecret, SecretOpReason.automatedOp("a job might do this"));
	}

	//NOTE: typically, CachedSecrets end up as private final fields that are built in a constructor.
	//There is no reason to use a CachedSecret in a local variable, since it will probably only be read once.
	public void readCachedSecrets(){
		//the provided default value will be returned in development, if no client returns a value
		CachedSecret<String> appSpecificSecret = cachedSecretFactory.cacheSecret(
				() -> SECRET_NAME,
				String.class,
				"development");
		CachedSecret<String> sharedSecret = cachedSecretFactory.cacheSharedSecretString(() -> SECRET_NAME);

		appSpecificSecret.get();
		sharedSecret.get();
	}

	public void beDangerousWithSecretClient(){
		SecretClient secretClient = secretClientSupplier.get();
		//namespacing and auditing not provided
		secretClient.delete("development/example-app/" + SECRET_NAME);
	}

}
