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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.redis.client.RedisOptions.RedisClientMode;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientId;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.codec.ByteArrayCodec;

@Singleton
public class RedisClientHolder{

	private final RedisOptions redisOptions;
	private final Map<ClientId,RedisClusterAsyncCommands<byte[],byte[]>> redisByClientId;

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

	public RedisClusterAsyncCommands<byte[],byte[]> get(ClientId clientId){
		return redisByClientId.get(clientId);
	}

	private RedisClusterAsyncCommands<byte[],byte[]> buildClient(ClientId clientId){
		if(getClientMode(clientId).isStandard()){
			return buildRegularClient(clientId);
		}
		// RedisClientType.CLUSTER
		return buildClusterClient(clientId);
	}

	public RedisClientMode getClientMode(ClientId clientId){
		return redisOptions.getClientMode(clientId.getName());
	}

	private RedisClusterAsyncCommands<byte[],byte[]> buildRegularClient(ClientId clientId){
		InetSocketAddress address = redisOptions.getEndpoint(clientId.getName()).get();
		RedisClient client = RedisClient.create(RedisURI.create(address.getHostName(), address.getPort()));
		return client.connect(ByteArrayCodec.INSTANCE).async();
	}

	private RedisClusterAsyncCommands<byte[],byte[]> buildClusterClient(ClientId clientId){
		RedisClientMode mode = redisOptions.getClientMode(clientId.getName());
		RedisClusterClient redisClusterClient;
		List<RedisURI> redisUris = new ArrayList<>();
		if(mode == RedisClientMode.AUTO_DISCOVERY){
			InetSocketAddress address = redisOptions.getEndpoint(clientId.getName()).get();
			String host = address.getHostName();
			int port = address.getPort();
			redisUris.add(RedisURI.create(host, port));
		}else{
			// mode == RedisClientModeType.MULTI_NODE
			Scanner.of(redisOptions.getNodes(clientId.getName()))
					.map(address -> RedisURI.create(address.getHostName(), address.getPort()))
					.distinct()
					.forEach(redisUris::add);
		}
		ClusterTopologyRefreshOptions refreshOptions = ClusterTopologyRefreshOptions.builder()
				.enableAllAdaptiveRefreshTriggers()
				.build();
		ClusterClientOptions clusterClientOptions = ClusterClientOptions.builder()
				.topologyRefreshOptions(refreshOptions)
				.validateClusterNodeMembership(false)
				.build();
		redisClusterClient = RedisClusterClient.create(redisUris);
		redisClusterClient.setOptions(clusterClientOptions);
		return redisClusterClient.connect(ByteArrayCodec.INSTANCE).async();
	}

}
