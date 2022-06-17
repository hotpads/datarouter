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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.enums.MappedEnum;
import io.datarouter.storage.client.ClientOptions;
import io.datarouter.storage.config.client.RedisGenericClientOptions.RedisGenericClientMode;

@Singleton
public class RedisOptions{

	private static final String PREFIX_REDIS = "redis.";
	public static final String PROP_endpoint = "endpoint";
	public static final String PROP_clientMode = "clientMode";
	public static final String PROP_numNodes = "numNodes";
	public static final String PROP_node = "node";

	@Inject
	private ClientOptions clientOptions;

	/*--------------------------------- redis -------------------------------*/

	public Optional<InetSocketAddress> getEndpoint(String clientName){
		return clientOptions.optInetSocketAddress(clientName, makeRedisKey(PROP_endpoint));
	}

	public RedisClientMode getClientMode(String clientName){
		return clientOptions.optString(clientName, makeRedisKey(PROP_clientMode))
				.map(RedisClientMode.BY_PERSISTENT_STRING::fromOrThrow)
				.orElseThrow(() -> new IllegalArgumentException("clientMode needs to be specified"));
	}

	public List<InetSocketAddress> getNodes(String clientName){
		return IntStream.range(0, getNumNodes(clientName))
				.mapToObj(index -> PROP_node + "." + index)
				.map(RedisOptions::makeRedisKey)
				.map(propertyKey -> clientOptions.optInetSocketAddress(clientName, propertyKey))
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	private Integer getNumNodes(String clientName){
		return clientOptions.getRequiredInteger(clientName, makeRedisKey(PROP_numNodes));
	}

	/*-------------------------------- helper -------------------------------*/

	public static String makeRedisKey(String propertyKey){
		return PREFIX_REDIS + propertyKey;
	}

	/*--------------------------------- mode --------------------------------*/


	public enum RedisClientMode{
		AUTO_DISCOVERY("autoDiscovery", true),
		MULTI_NODE("multiNode", true),
		STANDARD("standard", false);

		public static final MappedEnum<RedisClientMode,String> BY_PERSISTENT_STRING
				= new MappedEnum<>(values(), value -> value.persistentString);

		public final String persistentString;
		public final boolean isClustered;
		public final boolean isStandard;

		RedisClientMode(String persistentString, boolean isClustered){
			this.persistentString = persistentString;
			this.isClustered = isClustered;
			this.isStandard = !isClustered;
		}

		public static RedisClientMode fromGenericClientMode(RedisGenericClientMode genericMode){
			return switch(genericMode){
				case AUTO_DISCOVERY -> AUTO_DISCOVERY;
				case MULTI_NODE -> MULTI_NODE;
				case STANDARD -> STANDARD;
			};
		}

	}

}
