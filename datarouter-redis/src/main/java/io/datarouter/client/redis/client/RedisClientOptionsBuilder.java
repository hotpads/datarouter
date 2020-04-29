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

	public RedisClientOptionsBuilder withEndpoint(String endpoint){
		String optionKeySuffix = RedisOptions.makeRedisKey(RedisOptions.PROP_endpoint);
		String optionKey = makeKey(optionKeySuffix);
		properties.setProperty(optionKey, endpoint);
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
