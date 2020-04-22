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
package io.datarouter.client.redis.client;

import java.util.Properties;

import io.datarouter.client.redis.RedisClientType;
import io.datarouter.client.redis.client.RedisOptions.RedisClientMode;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.client.ClientOptionsBuilder;

public class RedisClientOptionsBuilder implements ClientOptionsBuilder{

	private final String clientIdName;
	private final Properties properties;

	public RedisClientOptionsBuilder(ClientId clientId){
		clientIdName = clientId.getName();
		properties = new Properties();
		properties.setProperty(ClientOptions.makeClientTypeKey(clientIdName), RedisClientType.NAME);
	}

	public RedisClientOptionsBuilder withNumServers(int numberOfServers){
		String optionKeySuffix = RedisOptions.makeRedisKey(RedisOptions.PROP_numServers);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, String.valueOf(numberOfServers));
		return this;
	}

	public RedisClientOptionsBuilder withServerIndexAndInetSocketAddress(int serverIndex, String inetSocketAddress){
		String optionKeySuffix = RedisOptions.makeRedisKey(RedisOptions.PROP_server + "." + serverIndex);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, inetSocketAddress);
		return this;
	}

	public RedisClientOptionsBuilder withClientMode(RedisClientMode clientMode){
		String optionKeySuffix = RedisOptions.makeRedisKey(RedisOptions.PROP_clientMode);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, clientMode.getPersistentString());
		return this;
	}

	public RedisClientOptionsBuilder withClusterEndpoint(String clusterEndpoint){
		String optionKeySuffix = RedisOptions.makeRedisKey(RedisOptions.PROP_clusterEndpoint);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, clusterEndpoint);
		return this;
	}

	@Override
	public Properties build(){
		return properties;
	}

	private String makeKey(String suffix){
		return ClientOptions.makeClientPrefixedKey(clientIdName, suffix);
	}

}
