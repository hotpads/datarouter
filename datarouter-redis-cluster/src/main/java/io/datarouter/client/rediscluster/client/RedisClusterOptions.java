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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.client.ClientOptions;
import io.datarouter.util.enums.DatarouterEnumTool;
import io.datarouter.util.enums.PersistentString;

@Singleton
public class RedisClusterOptions{

	private static final String PREFIX_REDIS = "redisCluster.";

	public static final String PROP_clientMode = "clientMode";
	public static final String PROP_clusterEndpoint = "clusterEndpoint";
	public static final String PROP_numNodes = "numNodes";
	public static final String PROP_node = "node";

	@Inject
	private ClientOptions clientOptions;

	public List<InetSocketAddress> getNodes(String clientName){
		return IntStream.range(0, getNumNodes(clientName))
				.mapToObj(index -> PROP_node + "." + index)
				.map(RedisClusterOptions::makeRedisKey)
				.map(propertyKey -> clientOptions.optInetSocketAddress(clientName, propertyKey))
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	public Optional<InetSocketAddress> getClusterEndpoint(String clientName){
		return clientOptions.optInetSocketAddress(clientName, makeRedisKey(PROP_clusterEndpoint));
	}

	public RedisClusterClientMode getClientMode(String clientName){
		return clientOptions.optString(clientName, makeRedisKey(PROP_clientMode))
				.map(RedisClusterClientMode::fromPersistentStringStatic)
				.orElseThrow(() -> new IllegalArgumentException("clientMode needs to be specified"));
	}

	protected static String makeRedisKey(String propertyKey){
		return PREFIX_REDIS + propertyKey;
	}

	private Integer getNumNodes(String clientName){
		return clientOptions.getRequiredInteger(clientName, makeRedisKey(PROP_numNodes));
	}

	public enum RedisClusterClientMode implements PersistentString{
		AUTO_DISCOVERY("autoDiscovery"),
		MULTI_NODE("multiNode"),
		;

		private final String persistentString;

		RedisClusterClientMode(String persistentString){
			this.persistentString = persistentString;
		}

		@Override
		public String getPersistentString(){
			return persistentString;
		}

		public static RedisClusterClientMode fromPersistentStringStatic(String string){
			return DatarouterEnumTool.getEnumFromStringOptional(values(), string)
					.orElseThrow();
		}

	}

}
