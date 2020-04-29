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
package io.datarouter.client.rediscluster.client;

import java.util.Properties;

import io.datarouter.client.rediscluster.RedisClusterClientType;
import io.datarouter.client.rediscluster.client.RedisClusterOptions.RedisClusterClientMode;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.client.ClientOptionsBuilder;

public class RedisClusterClientOptionsBuilder implements ClientOptionsBuilder{

	private final String clientIdName;
	private final Properties properties;

	public RedisClusterClientOptionsBuilder(ClientId clientId){
		clientIdName = clientId.getName();
		properties = new Properties();
		properties.setProperty(ClientOptions.makeClientTypeKey(clientIdName), RedisClusterClientType.NAME);
	}

	public RedisClusterClientOptionsBuilder withNumNodes(int numberOfNodes){
		String optionKeySuffix = RedisClusterOptions.makeRedisKey(RedisClusterOptions.PROP_numNodes);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, String.valueOf(numberOfNodes));
		return this;
	}

	public RedisClusterClientOptionsBuilder withNodeIndexAndHostAndPort(int nodeIndex,
			String inetSocketAddress){
		String optionKeySuffix = RedisClusterOptions.makeRedisKey(RedisClusterOptions.PROP_node + "." + nodeIndex);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, inetSocketAddress);
		return this;
	}

	public RedisClusterClientOptionsBuilder withClientMode(RedisClusterClientMode clientMode){
		String optionKeySuffix = RedisClusterOptions.makeRedisKey(RedisClusterOptions.PROP_clientMode);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, clientMode.getPersistentString());
		return this;
	}

	public RedisClusterClientOptionsBuilder withClusterEndpoint(String clusterEndpoint){
		String optionKeySuffix = RedisClusterOptions.makeRedisKey(RedisClusterOptions.PROP_clusterEndpoint);
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
