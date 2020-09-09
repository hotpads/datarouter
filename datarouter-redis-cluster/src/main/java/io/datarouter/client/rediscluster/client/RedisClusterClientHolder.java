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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.rediscluster.client.RedisClusterOptions.RedisClusterClientMode;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.ByteArrayCodec;

@Singleton
public class RedisClusterClientHolder{

	private final RedisClusterOptions redisOptions;
	private final Map<ClientId,StatefulRedisClusterConnection<byte[],byte[]>> redisByClientId;

	@Inject
	public RedisClusterClientHolder(RedisClusterOptions redisOptions){
		this.redisOptions = redisOptions;
		redisByClientId = new ConcurrentHashMap<>();
	}

	public void registerClient(ClientId clientId){
		if(redisByClientId.containsKey(clientId)){
			throw new RuntimeException(clientId + " already registered a JedisClient");
		}
		redisByClientId.put(clientId, buildClient(clientId));
	}

	public StatefulRedisClusterConnection<byte[],byte[]> get(ClientId clientId){
		return redisByClientId.get(clientId);
	}

	private StatefulRedisClusterConnection<byte[],byte[]> buildClient(ClientId clientId){
		RedisClusterClientMode mode = redisOptions.getClientMode(clientId.getName());
		if(mode == RedisClusterClientMode.AUTO_DISCOVERY){
			InetSocketAddress address = redisOptions.getClusterEndpoint(clientId.getName()).get();
			String host = address.getHostName();
			int port = address.getPort();
			return RedisClusterClient.create(RedisURI.create(host, port)).connect(ByteArrayCodec.INSTANCE);
		}
		// mode == RedisClusterClientMode.MULTI_NODE
		Set<RedisURI> nodes = Scanner.of(redisOptions.getNodes(clientId.getName()))
				.map(address -> RedisURI.create(address.getHostName(), address.getPort()))
				.collect(HashSet::new);

		ClusterTopologyRefreshOptions refreshOptions = ClusterTopologyRefreshOptions.builder()
				.enableAllAdaptiveRefreshTriggers()
				.enablePeriodicRefresh()
				.refreshPeriod(Duration.ofSeconds(10))
				.build();
		ClusterClientOptions clusterClientOptions = ClusterClientOptions.builder()
				.topologyRefreshOptions(refreshOptions)
				.build();
		RedisClusterClient redisClusterClient = RedisClusterClient.create(nodes);
		redisClusterClient.setOptions(clusterClientOptions);
		return redisClusterClient.connect(ByteArrayCodec.INSTANCE);
	}

}
