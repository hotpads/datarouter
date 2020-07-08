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
package io.datarouter.aws.sqs;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.json.JsonSerializer;
import io.datarouter.secret.client.SecretClientSupplier;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.web.handler.encoder.HandlerEncoder;

@Singleton
public class SqsOptions{
	private static final Logger logger = LoggerFactory.getLogger(SqsOptions.class);

	protected static final String PROP_region = "region";
	protected static final String PROP_credentialsLocation = "credentials.location";
	protected static final String PROP_accessKey = "accessKey";
	protected static final String PROP_secretKey = "secretKey";
	protected static final String PROP_namespace = "namespace";

	private final ConcurrentHashMap<String,Optional<SqsCredentialsDto>> clientCredentials = new ConcurrentHashMap<>();

	@Inject
	private ClientOptions clientOptions;
	@Inject
	private SecretClientSupplier secretClientSupplier;
	@Inject
	@Named(HandlerEncoder.DEFAULT_HANDLER_SERIALIZER)
	private JsonSerializer jsonSerializer;

	public String getAccessKey(String clientName){
		return readCredentialsSecret(clientName)
				.map(dto -> dto.accessKey)
				.orElseGet(() -> clientOptions.getRequiredString(clientName, PROP_accessKey));
	}

	public String getSecretKey(String clientName){
		return readCredentialsSecret(clientName)
				.map(dto -> dto.secretKey)
				.orElseGet(() -> clientOptions.getRequiredString(clientName, PROP_secretKey));
	}

	public String getRegion(String clientName){
		return clientOptions.getRequiredString(clientName, PROP_region);
	}

	private Optional<SqsCredentialsDto> readCredentialsSecret(String clientName){
		return clientCredentials.computeIfAbsent(clientName, $ -> {
			Optional<String> optionalCredentialsLocation = clientOptions.optString(clientName,
					PROP_credentialsLocation);
			if(optionalCredentialsLocation.isEmpty()){
				logger.warn("credentialsLocation not specified");
			}
			return optionalCredentialsLocation.map(credentialsLocation -> {
				try{
					SqsCredentialsDto result = jsonSerializer.deserialize(secretClientSupplier.get().read(
							credentialsLocation).getValue(), SqsCredentialsDto.class);
					logger.info("using secret at credentialsLocation={}", credentialsLocation);
					return result;
				}catch(RuntimeException e){
					logger.error("Failed to locate credentialsLocation=" + credentialsLocation + " for clientName="
							+ clientName, e);
					return null;
				}
			});
		});
	}

}
