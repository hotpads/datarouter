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
package io.datarouter.gcp.spanner.client;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

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

@Singleton
public class SpannerClientOptions{
	private static final Logger logger = LoggerFactory.getLogger(SpannerClientOptions.class);

	private static final String PREFIX_SPANNER = "spanner.";

	protected static final String PROP_projectId = "projectId";
	protected static final String PROP_instanceId = "instanceId";
	protected static final String PROP_databaseName = "databaseName";
	protected static final String PROP_credentialsFileLocation = "credentialsFileLocation";
	protected static final String PROP_credentialsSecretLocation = "credentialsSecretLocation";
	protected static final String PROP_maxSessions = "maxSessions";
	protected static final String PROP_numChannels = "numChannels";

	private static final int DEFAULT_MAX_SESSIONS = 400;
	private static final int DEFAULT_NUM_CHANNELS = 4;

	@Inject
	private ClientOptions clientOptions;
	@Inject
	private SecretService secretService;

	public String projectId(String clientName){
		return clientOptions.getRequiredString(clientName, makeSpannerKey(PROP_projectId));
	}

	public String instanceId(String clientName){
		return clientOptions.getRequiredString(clientName, makeSpannerKey(PROP_instanceId));
	}

	public String findProjectId(String clientName){
		return clientOptions.optString(clientName, makeSpannerKey(PROP_projectId)).orElse("");
	}

	public String findInstanceId(String clientName){
		return clientOptions.optString(clientName, makeSpannerKey(PROP_instanceId)).orElse("");
	}

	public String databaseName(String clientName){
		return clientOptions.getRequiredString(clientName, makeSpannerKey(PROP_databaseName));
	}

	public int maxSessions(String clientName){
		return clientOptions.optString(clientName, makeSpannerKey(PROP_maxSessions))
				.map(Integer::valueOf)
				.orElse(DEFAULT_MAX_SESSIONS);
	}

	public int numChannels(String clientName){
		return clientOptions.optString(clientName, makeSpannerKey(PROP_numChannels))
				.map(Integer::valueOf)
				.orElse(DEFAULT_NUM_CHANNELS);
	}

	public Credentials credentials(String clientName){
		InputStream inputStream = readCredentialsSecret(clientName)
				.or(() -> readCredentialsFile(clientName))
				.orElseThrow(() -> new RuntimeException("no spanner credentials configuration found"));
		try{
			return GoogleCredentials.fromStream(inputStream);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	protected static String makeSpannerKey(String propertyKey){
		return PREFIX_SPANNER + propertyKey;
	}

	private Optional<InputStream> readCredentialsFile(String clientName){
		Optional<String> optProvided = clientOptions.optString(clientName, makeSpannerKey(
				PROP_credentialsFileLocation));
		if(optProvided.isEmpty()){
			logger.warn("{} not specified", PROP_credentialsFileLocation);
			return Optional.empty();
		}
		return optProvided.map(provided -> {
			String corrected = provided.replace("~", SystemTool.getUserHome());
			if(ObjectTool.notEquals(provided, corrected)){
				logger.warn("updated credentialsLocation from {} to {}", provided, corrected);
			}
			return corrected;
		}).map(filename -> {
			try{
				return new FileInputStream(filename);
			}catch(FileNotFoundException e){
				throw new RuntimeException(e);
			}
		});
	}

	private Optional<InputStream> readCredentialsSecret(String clientName){
		Optional<String> optSecretLocation = clientOptions.optString(clientName, makeSpannerKey(
				PROP_credentialsSecretLocation));
		if(optSecretLocation.isEmpty()){
			logger.warn("{} not specified", PROP_credentialsSecretLocation);
			return Optional.empty();
		}
		return optSecretLocation
				.map($ -> {
					SecretOpConfig config = SecretOpConfig.builder(
							SecretOpReason.automatedOp(this.getClass().getSimpleName()))
							.useSharedNamespace()
							.disableRecording()
							.disableSerialization()
							.build();
					return secretService.read($, String.class, config);
				}).map(str -> str.getBytes(StandardCharsets.UTF_8))
				.map(ByteArrayInputStream::new);
	}

}
