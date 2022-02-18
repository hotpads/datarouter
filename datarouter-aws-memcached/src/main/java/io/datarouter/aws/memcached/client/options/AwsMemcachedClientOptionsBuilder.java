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
package io.datarouter.aws.memcached.client.options;

import java.util.Optional;
import java.util.Properties;

import io.datarouter.aws.memcached.AwsMemcachedClientType;
import io.datarouter.aws.memcached.client.MemcachedClientMode;
import io.datarouter.client.memcached.client.options.MemcachedClientOptionsBuilder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.client.MemcachedGenericClientOptions;

public class AwsMemcachedClientOptionsBuilder extends MemcachedClientOptionsBuilder{

	public AwsMemcachedClientOptionsBuilder(ClientId clientId){
		super(clientId, AwsMemcachedClientType.NAME);
	}

	public AwsMemcachedClientOptionsBuilder(MemcachedGenericClientOptions genericOptions){
		this(genericOptions.clientId);
		withClientMode(MemcachedClientMode.fromGenericClientMode(genericOptions.clientMode));
		Optional.ofNullable(genericOptions.servers)
				.ifPresent(this::withServers);
		Optional.ofNullable(genericOptions.clusterEndpoint)
				.ifPresent(this::withClusterEndpoint);
	}

	public AwsMemcachedClientOptionsBuilder withClientMode(MemcachedClientMode clientMode){
		String optionKeySuffix = AwsMemcachedOptions.makeAwsMemcachedKey(AwsMemcachedOptions.PROP_clientMode);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, clientMode.getPersistentString());
		return this;
	}

	public AwsMemcachedClientOptionsBuilder withClusterEndpoint(String clusterEndpoint){
		String optionKeySuffix = AwsMemcachedOptions.makeAwsMemcachedKey(AwsMemcachedOptions.PROP_clusterEndpoint);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, clusterEndpoint);
		return this;
	}

	@Override
	public Properties build(){
		return properties;
	}

}
