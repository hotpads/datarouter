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
package io.datarouter.aws.secretsmanager;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.secret.client.SecretClient;
import io.datarouter.secret.client.SecretClientSupplier;
import io.datarouter.secret.client.local.LocalStorageSecretClient;
import io.datarouter.secret.service.SecretStageDetector;

@Singleton
public class AwsWithDevLocalStorageSecretClientSupplier implements SecretClientSupplier{

	@Inject
	private AwsSecretClient awsSecretClient;
	@Inject
	private LocalStorageSecretClient localStorageSecretClient;
	@Inject
	private SecretStageDetector secretStageDetector;

	@Override
	public SecretClient get(){
		SecretClient clientToReturn = localStorageSecretClient;
		if(!secretStageDetector.mightBeDevelopment()){
			clientToReturn = awsSecretClient;
		}
		return clientToReturn;
	}

}
