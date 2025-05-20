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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.lang.ReflectionTool;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;

@Singleton
public class AwsSecretClientCredentialsHolder{
	private static final Logger logger = LoggerFactory.getLogger(AwsSecretClientCredentialsHolder.class);

	public static final String PROFILE_NAME = "secretsmanager";

	public AwsCredentialsProvider getCredentialsProvider(){
		AwsCredentialsProviderChain provider = AwsCredentialsProviderChain.builder()
				.addCredentialsProvider(SystemPropertyCredentialsProvider.create())
				.addCredentialsProvider(EnvironmentVariableCredentialsProvider.create())
				.addCredentialsProvider(ProfileCredentialsProvider.create(PROFILE_NAME))
				.build();
		try{
			AwsCredentials credentials = provider.resolveCredentials();
			Object usedProvider = ReflectionTool.get("lastUsedProvider", provider);
			logger.warn("using accessKey={} from provider={}",
					credentials.accessKeyId(),
					usedProvider.getClass().getSimpleName());
			return provider;
		}catch(SdkClientException e){
			throw new RuntimeException("failed to find AWS credentials.");
		}
	}

}
