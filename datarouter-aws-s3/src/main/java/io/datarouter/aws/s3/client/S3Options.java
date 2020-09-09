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
package io.datarouter.aws.s3.client;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.aws.s3.SerializableStaticAwsCredentialsProviderProvider;
import io.datarouter.aws.s3.SerializableStaticAwsCredentialsProviderProvider.S3CredentialsDto;
import io.datarouter.secret.service.SecretNamespacer;
import io.datarouter.secret.service.SecretOpReason;
import io.datarouter.secret.service.SecretService;
import io.datarouter.storage.client.ClientOptions;

@Singleton
public class S3Options{
	private static final Logger logger = LoggerFactory.getLogger(S3Options.class);

	protected static final String PROP_credentialsLocation = "credentials.location";
	protected static final String PROP_accessKey = "accessKey";
	protected static final String PROP_secretKey = "secretKey";

	private final ConcurrentHashMap<String,Optional<S3CredentialsDto>> clientCredentials = new ConcurrentHashMap<>();

	@Inject
	private ClientOptions clientOptions;
	@Inject
	private SecretNamespacer secretNamespacer;
	@Inject
	private SecretService secretService;

	public SerializableStaticAwsCredentialsProviderProvider makeCredentialsProvider(String clientName){
		String accessKey = getAccessKey(clientName);
		String secretKey = getSecretKey(clientName);
		return new SerializableStaticAwsCredentialsProviderProvider(new S3CredentialsDto(accessKey, secretKey));
	}

	private String getAccessKey(String clientName){
		return readCredentialsSecret(clientName)
				.map(dto -> dto.accessKey)
				.orElseGet(() -> clientOptions.getRequiredString(clientName, PROP_accessKey));
	}

	private String getSecretKey(String clientName){
		return readCredentialsSecret(clientName)
				.map(dto -> dto.secretKey)
				.orElseGet(() -> clientOptions.getRequiredString(clientName, PROP_secretKey));
	}

	private Optional<S3CredentialsDto> readCredentialsSecret(String clientName){
		return clientCredentials.computeIfAbsent(clientName, $ -> {
			Optional<String> optionalCredentialsLocation = clientOptions.optString(clientName,
					PROP_credentialsLocation);
			if(optionalCredentialsLocation.isEmpty()){
				logger.warn("credentialsLocation not specified");
			}
			return optionalCredentialsLocation.map(credentialsLocation -> {
				String namespacedLocationForLogs = secretNamespacer.sharedNamespaced(credentialsLocation);
				try{
					S3CredentialsDto result = secretService.readShared(credentialsLocation, S3CredentialsDto.class,
							SecretOpReason.automatedOp(this.getClass().getName()));
					logger.info("using secret at credentialsLocation={}", namespacedLocationForLogs);
					return result;
				}catch(RuntimeException e){
					logger.error("Failed to locate credentialsLocation={} for clientName={}", namespacedLocationForLogs,
							clientName, e);
					return null;
				}
			});
		});
	}

}
