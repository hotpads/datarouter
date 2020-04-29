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

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.rediscluster.client.RedisClusterOptions.RedisClusterClientMode;
import io.datarouter.storage.client.ClientId;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

@Singleton
public class JedisClusterHolder{

	private static final int CONNECTION_TIMEOUT = (int) Duration.ofSeconds(3).toMillis();

	@Inject
	private RedisClusterOptions redisOptions;

	private final Map<ClientId,JedisCluster> jedisByClient = new ConcurrentHashMap<>();

	public void registerClient(ClientId clientId){
		if(jedisByClient.containsKey(clientId)){
			throw new RuntimeException(clientId + " already registered a JedisClient");
		}
		jedisByClient.put(clientId, buildClient(clientId));
	}

	public JedisCluster get(ClientId clientId){
		return jedisByClient.get(clientId);
	}

	private JedisCluster buildClient(ClientId clientId){
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(128);
		poolConfig.setMaxIdle(128);
		poolConfig.setMinIdle(16);

		RedisClusterClientMode mode = redisOptions.getClientMode(clientId.getName());
		if(mode == RedisClusterClientMode.AUTO_DISCOVERY){
			InetSocketAddress address = redisOptions.getClusterEndpoint(clientId.getName()).get();
			String host = address.getHostName();
			int port = address.getPort();
			return new JedisCluster(new HostAndPort(host, port), CONNECTION_TIMEOUT, poolConfig);
		}
		// mode == RedisClusterClientMode.MULTI_NODE
		Set<HostAndPort> nodes = redisOptions.getNodes(clientId.getName()).stream()
				.map(address -> new HostAndPort(address.getHostName(), address.getPort()))
				.collect(Collectors.toSet());
		return new JedisCluster(nodes, CONNECTION_TIMEOUT, poolConfig);
	}

}
