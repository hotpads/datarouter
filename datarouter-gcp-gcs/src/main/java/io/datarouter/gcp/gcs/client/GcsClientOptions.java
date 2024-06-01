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
package io.datarouter.gcp.gcs.client;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;

import io.datarouter.secret.op.SecretOpConfig;
import io.datarouter.secret.op.SecretOpReason;
import io.datarouter.secret.service.SecretService;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.util.SystemTool;
import io.datarouter.util.lang.ObjectTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GcsClientOptions{
	private static final Logger logger = LoggerFactory.getLogger(GcsClientOptions.class);

	private static final String PREFIX_GCS = "storage.";

	protected static final String PROP_projectId = "projectId";
	protected static final String PROP_instanceId = "instanceId";
	protected static final String PROP_credentialsFileLocation = "credentialsFileLocation";
	public static final String PROP_credentialsSecretLocation = "credentialsSecretLocation";

	@Inject
	private ClientOptions clientOptions;
	@Inject
	private SecretService secretService;

	public String projectId(String clientName){
		return clientOptions.getRequiredString(clientName, makeGcsKey(PROP_projectId));
	}

	public String instanceId(String clientName){
		return clientOptions.getRequiredString(clientName, makeGcsKey(PROP_instanceId));
	}

	public Credentials credentials(String clientName){
		InputStream inputStream = readCredentialsSecret(clientName)
				.or(() -> readCredentialsFile(clientName))
				.orElseThrow(() -> new RuntimeException("no GCS credentials configuration found"));
		try{
			return GoogleCredentials.fromStream(inputStream);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	private Optional<InputStream> readCredentialsFile(String clientName){
		Optional<String> optProvided = clientOptions.optString(
				clientName,
				makeGcsKey(PROP_credentialsFileLocation));
		if(optProvided.isEmpty()){
			logger.warn("{} not specified", PROP_credentialsFileLocation);
			return Optional.empty();
		}
		return optProvided
				.map(provided -> {
					String corrected = provided.replace("~", SystemTool.getUserHome());
					if(ObjectTool.notEquals(provided, corrected)){
						logger.warn("updated credentialsLocation from {} to {}", provided, corrected);
					}
					return corrected;
				})
				.map(filename -> {
					try{
						return new FileInputStream(filename);
					}catch(FileNotFoundException e){
						throw new RuntimeException(e);
					}
				});
	}

	private Optional<InputStream> readCredentialsSecret(String clientName){
		Optional<String> optSecretLocation = clientOptions.optString(
				clientName,
				makeGcsKey(PROP_credentialsSecretLocation));
		if(optSecretLocation.isEmpty()){
			logger.warn("{} not specified", PROP_credentialsSecretLocation);
			return Optional.empty();
		}
		return optSecretLocation
				.map($ -> {
					SecretOpConfig config = SecretOpConfig.builder(
							SecretOpReason.automatedOp(this.getClass().getSimpleName()))
							.useSharedNamespace()
							.disableSerialization()
							.build();
					return secretService.read($, String.class, config);
				})
				.map(str -> str.getBytes(StandardCharsets.UTF_8))
				.map(ByteArrayInputStream::new);
	}

	public static String makeGcsKey(String propertyKey){
		return PREFIX_GCS + propertyKey;
	}

}
