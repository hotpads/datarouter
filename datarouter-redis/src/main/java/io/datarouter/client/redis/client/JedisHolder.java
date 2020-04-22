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
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.redis.client.RedisOptions.RedisClientMode;
import io.datarouter.storage.client.ClientId;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Singleton
public class JedisHolder{

	private static final int CONNECTION_TIMEOUT = (int) Duration.ofSeconds(3).toMillis();

	@Inject
	private RedisOptions redisOptions;

	private final Map<ClientId,JedisPool> jedisByClient = new ConcurrentHashMap<>();

	public void registerClient(ClientId clientId){
		if(jedisByClient.containsKey(clientId)){
			throw new RuntimeException(clientId + " already registered a JedisClient");
		}
		String host = buildClient(clientId).get(0).getHostName();
		int port = buildClient(clientId).get(0).getPort();
		JedisPoolConfig poolConfig = buildPoolConfig();
		JedisPool jedisPool = new JedisPool(poolConfig, host, port, CONNECTION_TIMEOUT);
		jedisByClient.put(clientId, jedisPool);
	}

	public JedisPool get(ClientId clientId){
		return jedisByClient.get(clientId);
	}

	private List<InetSocketAddress> buildClient(ClientId clientId){
		List<InetSocketAddress> addresses;
		RedisClientMode clientMode = redisOptions.getClientMode(clientId.getName());
		if(clientMode == RedisClientMode.DYNAMIC){
			addresses = Arrays.asList(redisOptions.getClusterEndpoint(clientId.getName()).get());
		}else{
			addresses = redisOptions.getServers(clientId.getName());
		}
		return addresses;
	}

	private JedisPoolConfig buildPoolConfig(){
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(128);
		poolConfig.setMaxIdle(128);
		poolConfig.setMinIdle(16);
		return poolConfig;
	}

}
