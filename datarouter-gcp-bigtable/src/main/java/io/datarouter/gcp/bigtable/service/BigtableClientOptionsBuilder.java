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
package io.datarouter.gcp.bigtable.service;

import java.util.Optional;
import java.util.Properties;

import io.datarouter.gcp.bigtable.config.BigtableClientType;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.client.ClientOptionsBuilder;
import io.datarouter.storage.config.client.BigtableGenericClientOptions;

public class BigtableClientOptionsBuilder implements ClientOptionsBuilder{

	private static final String PREFIX_bigtable = "bigtable.";

	protected static final String PROP_projectId = "projectId";
	protected static final String PROP_instanceId = "instanceId";
	protected static final String PROP_credentialsFileLocation = "credentialsFileLocation";
	protected static final String PROP_credentialsSecretLocation = "credentialsSecretLocation";

	private final String clientIdName;
	private final Properties properties;

	public BigtableClientOptionsBuilder(ClientId clientId){
		this(clientId, BigtableClientType.NAME);
	}

	public BigtableClientOptionsBuilder(ClientId clientId, String clientTypeName){
		clientIdName = clientId.getName();
		properties = new Properties();
		properties.setProperty(ClientOptions.makeClientTypeKey(clientIdName), clientTypeName);
	}

	public BigtableClientOptionsBuilder(BigtableGenericClientOptions genericOptions){
		this(genericOptions.clientId);
		withProjectId(genericOptions.projectId);
		withInstanceId(genericOptions.instanceId);
		Optional.ofNullable(genericOptions.credentialsFileLocation)
				.ifPresent(this::withCredentialsFileLocation);
		Optional.ofNullable(genericOptions.credentialsSecretLocation)
				.ifPresent(this::withCredentialsSecretLocation);
	}

	public BigtableClientOptionsBuilder withProjectId(String projectId){
		String optionKeySuffix = makeBigtableKey(PROP_projectId);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, projectId);
		return this;
	}

	public BigtableClientOptionsBuilder withInstanceId(String instanceId){
		String optionKeySuffix = makeBigtableKey(PROP_instanceId);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, instanceId);
		return this;
	}

	public BigtableClientOptionsBuilder withCredentialsFileLocation(String credentialsFileLocation){
		String optionKeySuffix = makeBigtableKey(PROP_credentialsFileLocation);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, credentialsFileLocation);
		return this;
	}

	public BigtableClientOptionsBuilder withCredentialsSecretLocation(String credentialsSecretLocation){
		String optionKeySuffix = makeBigtableKey(PROP_credentialsSecretLocation);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, credentialsSecretLocation);
		return this;
	}

	protected String makeKey(String suffix){
		return ClientOptions.makeClientPrefixedKey(clientIdName, suffix);
	}

	public static String makeBigtableKey(String propertyKey){
		return PREFIX_bigtable + propertyKey;
	}

	@Override
	public Properties build(){
		return properties;
	}

}
