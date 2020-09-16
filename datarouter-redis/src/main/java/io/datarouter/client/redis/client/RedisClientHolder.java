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

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.client.ClientId;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;

@Singleton
public class RedisClientHolder{

	private final RedisOptions redisOptions;
	private final Map<ClientId,StatefulRedisConnection<byte[],byte[]>> redisByClientId;

	@Inject
	public RedisClientHolder(RedisOptions redisOptions){
		this.redisOptions = redisOptions;
		redisByClientId = new ConcurrentHashMap<>();
	}

	public void registerClient(ClientId clientId){
		if(redisByClientId.containsKey(clientId)){
			throw new RuntimeException(clientId + " already registered a RedisClient");
		}
		redisByClientId.put(clientId, buildClient(clientId));
	}

	public StatefulRedisConnection<byte[],byte[]> get(ClientId clientId){
		return redisByClientId.get(clientId);
	}

	private StatefulRedisConnection<byte[],byte[]> buildClient(ClientId clientId){
		InetSocketAddress address = redisOptions.getEndpoint(clientId.getName());
		RedisClient client = RedisClient.create(RedisURI.create(address.getHostName(), address.getPort()));
		return client.connect(ByteArrayCodec.INSTANCE);
	}

}
