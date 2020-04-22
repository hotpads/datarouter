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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.client.ClientOptions;

@Singleton
public class RedisOptions{

	private static final String PREFIX_REDIS = "redis.";

	public static final String PROP_clientMode = "clientMode";
	public static final String PROP_clusterEndpoint = "clusterEndpoint";
	public static final String PROP_numServers = "numServers";
	public static final String PROP_server = "server";

	@Inject
	private ClientOptions clientOptions;

	public List<InetSocketAddress> getServers(String clientName){
		return IntStream.range(0, getNumServers(clientName))
				.mapToObj(index -> makeRedisKey(PROP_server + "." + index))
				.map(propertyKey -> clientOptions.optInetSocketAddress(clientName, propertyKey))
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	public Optional<InetSocketAddress> getClusterEndpoint(String clientName){
		return clientOptions.optInetSocketAddress(clientName, makeRedisKey(PROP_clusterEndpoint));
	}

	public RedisClientMode getClientMode(String clientName){
		return clientOptions.optString(clientName, makeRedisKey(PROP_clientMode))
				.filter(RedisClientMode.DYNAMIC.getPersistentString()::equals)
				.map($ -> RedisClientMode.DYNAMIC)
				.orElse(RedisClientMode.STATIC);
	}

	protected static String makeRedisKey(String propertyKey){
		return PREFIX_REDIS + propertyKey;
	}

	private Integer getNumServers(String clientName){
		return clientOptions.getRequiredInteger(clientName, makeRedisKey(PROP_numServers));
	}

	public enum RedisClientMode{
		STATIC("static"),
		DYNAMIC("dynamic");

		private String persistentString;

		RedisClientMode(String persistentString){
			this.persistentString = persistentString;
		}

		public String getPersistentString(){
			return persistentString;
		}

	}

}
