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
package io.datarouter.aws.secretsmanager;

import com.amazonaws.regions.Regions;

import io.datarouter.secret.client.SecretClient;
import io.datarouter.secret.client.SecretClient.SecretClientSupplier;
import io.datarouter.web.config.AwsSupport;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class AwsSecretClientSupplier implements SecretClientSupplier{

	public static final String REGION = Regions.US_EAST_1.getName();//TODO make this configurable

	@Inject
	private AwsSecretClientCredentialsHolder awsCredentialsSupplier;
	@Inject
	private AwsSupport awsSupport;

	private AwsSecretClient awsSecretClient;

	@Override
	public SecretClient get(){
		if(awsSecretClient == null){
			synchronized(this){
				if(awsSecretClient == null){
					awsSecretClient = new AwsSecretClient(
							awsCredentialsSupplier.getCredentialsProvider(),
							REGION,
							awsSupport);
				}
			}
		}
		return awsSecretClient;
	}

}
