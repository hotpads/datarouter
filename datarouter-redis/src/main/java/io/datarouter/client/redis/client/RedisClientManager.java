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
package io.datarouter.client.redis.client;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.client.redis.RedisClientType;
import io.datarouter.client.redis.client.RedisOptions.RedisClientMode;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.util.singletonsupplier.SingletonSupplier;
import io.datarouter.util.timer.PhaseTimer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class RedisClientManager extends BaseClientManager{
	private static final Logger logger = LoggerFactory.getLogger(RedisClientManager.class);

	@Inject
	private RedisClientType clientType;
	@Inject
	private RedisOptions options;
	@Inject
	private RedisClientHolder holder;

	@Override
	protected void safeInitClient(ClientId clientId){
		holder.registerClient(clientId, buildClient(clientId));
	}

	@Override
	public void shutdown(ClientId clientId){
		// holder.get(clientId).shutdown(false);
	}

	public DatarouterRedisClient getClient(ClientId clientId){
		initClient(clientId);
		return holder.get(clientId);
	}

	private DatarouterRedisClient buildClient(ClientId clientId){
		if(options.getClientMode(clientId.getName()).isStandard){
			return buildRegularClient(clientId);
		}
		// RedisClientType.CLUSTER
		return buildClusterClient(clientId);
	}

	private DatarouterRedisClient buildRegularClient(ClientId clientId){
		var timer = new PhaseTimer(clientId.getName());
		InetSocketAddress address = options.getEndpoint(clientId.getName()).get();
		RedisClient client = RedisClient.create(RedisURI.create(address.getHostName(), address.getPort()));
		RedisAsyncCommands<byte[],byte[]> lettuceClient = client
				.connect(ByteArrayCodec.INSTANCE)
				.async();
		logger.warn("{}", timer.add("buildRegularClient"));
		return new DatarouterRedisClient(clientType, clientId, lettuceClient);
	}

	private DatarouterRedisClient buildClusterClient(ClientId clientId){
		var timer = new PhaseTimer(clientId.getName());
		RedisClientMode mode = options.getClientMode(clientId.getName());
		RedisClusterClient redisClusterClient;
		List<RedisURI> redisUris = new ArrayList<>();
		if(mode == RedisClientMode.AUTO_DISCOVERY){
			InetSocketAddress address = options.getEndpoint(clientId.getName()).get();
			String host = address.getHostName();
			int port = address.getPort();
			redisUris.add(RedisURI.create(host, port));
		}else{
			// mode == RedisClientModeType.MULTI_NODE
			Scanner.of(options.getNodes(clientId.getName()))
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
		RedisAdvancedClusterAsyncCommands<byte[],byte[]> lettuceClient = redisClusterClient
				.connect(ByteArrayCodec.INSTANCE)
				.async();
		logger.warn("{}", timer.add("buildClusterClient"));
		return new DatarouterRedisClient(clientType, clientId, lettuceClient);
	}

	public Supplier<DatarouterRedisClient> getLazyClient(ClientId clientId){
		return SingletonSupplier.of(() -> getClient(clientId));
	}

}
