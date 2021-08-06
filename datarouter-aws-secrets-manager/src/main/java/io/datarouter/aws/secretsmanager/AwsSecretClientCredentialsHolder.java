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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import io.datarouter.util.string.StringTool;

public interface AwsSecretClientCredentialsHolder{

	//TODO change interface after getting rid of hardcoded provider
	Optional<AWSCredentialsProvider> getDevCredentialsProvider();
	Optional<AWSCredentialsProvider> getStagingCredentialsProvider();
	Optional<AWSCredentialsProvider> getProdCredentialsProvider();

	class HardcodedAwsSecretClientCredentialsHolder implements AwsSecretClientCredentialsHolder{

		private final String devAccessKey;
		private final String devSecretKey;
		private final String stagingAccessKey;
		private final String stagingSecretKey;
		private final String prodAccessKey;
		private final String prodSecretkey;

		public HardcodedAwsSecretClientCredentialsHolder(String devAccessKey, String devSecretKey,
				String stagingAccessKey, String stagingSecretKey, String prodAccessKey, String prodSecretkey){
			this.devAccessKey = devAccessKey;
			this.devSecretKey = devSecretKey;
			this.stagingAccessKey = stagingAccessKey;
			this.stagingSecretKey = stagingSecretKey;
			this.prodAccessKey = prodAccessKey;
			this.prodSecretkey = prodSecretkey;
		}

		@Override
		public Optional<AWSCredentialsProvider> getDevCredentialsProvider(){
			return buildCredentials(devAccessKey, devSecretKey);
		}

		@Override
		public Optional<AWSCredentialsProvider> getStagingCredentialsProvider(){
			return buildCredentials(stagingAccessKey, stagingSecretKey);
		}

		@Override
		public Optional<AWSCredentialsProvider> getProdCredentialsProvider(){
			return buildCredentials(prodAccessKey, prodSecretkey);
		}

		private Optional<AWSCredentialsProvider> buildCredentials(String accessKey, String secretKey){
			if(StringTool.isNullOrEmptyOrWhitespace(accessKey) || StringTool.isNullOrEmptyOrWhitespace(secretKey)){
				return Optional.empty();
			}
			return Optional.of(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)));
		}

	}

	class DefaultAwsSecretClientCredentialsHolder implements AwsSecretClientCredentialsHolder{
		private static final Logger logger = LoggerFactory.getLogger(DefaultAwsSecretClientCredentialsHolder.class);

		public static final String PROFILE_NAME = "secretsmanager";

		@Override
		public Optional<AWSCredentialsProvider> getDevCredentialsProvider(){
			return getCredentialsProvider();
		}

		@Override
		public Optional<AWSCredentialsProvider> getStagingCredentialsProvider(){
			return getCredentialsProvider();
		}

		@Override
		public Optional<AWSCredentialsProvider> getProdCredentialsProvider(){
			return getCredentialsProvider();
		}

		private Optional<AWSCredentialsProvider> getCredentialsProvider(){
			AWSCredentialsProvider provider = new AWSCredentialsProviderChain(
					new SystemPropertiesCredentialsProvider(),
					new EnvironmentVariableCredentialsProvider(),
					new ProfileCredentialsProvider(PROFILE_NAME));
			try{
				AWSCredentials credentials = provider.getCredentials();
				logger.info("using accessKey={}", credentials.getAWSAccessKeyId());
				return Optional.of(provider);
			}catch(SdkClientException e){
				throw new RuntimeException("failed to find AWS credentials.");
			}
		}

	}

}
