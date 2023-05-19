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

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

@Singleton
public class AwsSecretClientCredentialsHolder{
	private static final Logger logger = LoggerFactory.getLogger(AwsSecretClientCredentialsHolder.class);

	public static final String PROFILE_NAME = "secretsmanager";

	public AWSCredentialsProvider getCredentialsProvider(){
		AWSCredentialsProvider provider = new AWSCredentialsProviderChain(
				new SystemPropertiesCredentialsProvider(),
				new EnvironmentVariableCredentialsProvider(),
				new ProfileCredentialsProvider(PROFILE_NAME));
		try{
			AWSCredentials credentials = provider.getCredentials();
			logger.warn("using accessKey={}", credentials.getAWSAccessKeyId());
			return provider;
		}catch(SdkClientException e){
			throw new RuntimeException("failed to find AWS credentials.");
		}
	}

}
