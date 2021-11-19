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
package io.datarouter.gcp.bigtable.client;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.bigtable.hbase.BigtableOptionsFactory;

import io.datarouter.client.hbase.client.HBaseOptions;
import io.datarouter.secret.op.SecretOpConfig;
import io.datarouter.secret.op.SecretOpReason;
import io.datarouter.secret.service.SecretService;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.util.SystemTool;
import io.datarouter.util.lang.ObjectTool;
import io.datarouter.util.tuple.Twin;

@Singleton
public class BigTableOptions extends HBaseOptions{
	private static final Logger logger = LoggerFactory.getLogger(BigTableOptions.class);

	private static final String PREFIX_bigtable = "bigtable.";

	protected static final String PROP_projectId = "projectId";
	protected static final String PROP_instanceId = "instanceId";
	protected static final String PROP_credentialsFileLocation = "credentialsFileLocation";
	protected static final String PROP_credentialsSecretLocation = "credentialsSecretLocation";

	@Inject
	private ClientOptions clientOptions;
	@Inject
	private SecretService secretService;

	public String projectId(String clientName){
		return clientOptions.getRequiredString(clientName, makeBigtableKey(PROP_projectId));
	}

	public String instanceId(String clientName){
		return clientOptions.getRequiredString(clientName, makeBigtableKey(PROP_instanceId));
	}

	public String findProjectId(String clientName){
		return clientOptions.optString(clientName, makeBigtableKey(PROP_projectId)).orElse("");
	}

	public String findInstanceId(String clientName){
		return clientOptions.optString(clientName, makeBigtableKey(PROP_instanceId)).orElse("");
	}

	public Twin<String> bigtableConfigurationCredentialsKeyValue(String clientName){
		return readCredentialsSecretKeyValue(clientName)
				.or(() -> readCredentialsFileKeyValue(clientName))
				.orElseThrow(() -> new RuntimeException("no bigtable credentials configuration found"));
	}

	public Optional<Twin<String>> readCredentialsFileKeyValue(String clientName){
		Optional<String> optProvided = clientOptions.optString(clientName, makeBigtableKey(
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
		}).map($ -> new Twin<>(BigtableOptionsFactory.BIGTABLE_SERVICE_ACCOUNT_JSON_KEYFILE_LOCATION_KEY, $));
	}

	public Optional<Twin<String>> readCredentialsSecretKeyValue(String clientName){
		Optional<String> optSecretLocation = clientOptions.optString(clientName, makeBigtableKey(
				PROP_credentialsSecretLocation));
		if(optSecretLocation.isEmpty()){
			logger.warn("{} not specified", PROP_credentialsSecretLocation);
			return Optional.empty();
		}
		return optSecretLocation
				.map($ -> {
					var config = SecretOpConfig.builder(SecretOpReason.automatedOp(this.getClass().getSimpleName()))
							.useSharedNamespace()
							.disableRecording()
							.disableSerialization()
							.build();
					return secretService.read($, String.class, config);
				}).map($ -> new Twin<>(BigtableOptionsFactory.BIGTABLE_SERVICE_ACCOUNT_JSON_VALUE_KEY, $));
	}

	protected static String makeBigtableKey(String propertyKey){
		return PREFIX_bigtable + propertyKey;
	}

}
