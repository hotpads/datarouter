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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.client.ClientId;
import redis.clients.jedis.Jedis;

@Singleton
public class JedisHolder{

	@Inject
	private RedisOptions redisOptions;

	private final Map<ClientId,Jedis> jedisByClient = new ConcurrentHashMap<>();

	public void registerClient(ClientId clientId){
		if(jedisByClient.containsKey(clientId)){
			throw new RuntimeException(clientId + " already registered a JedisClient");
		}
		Jedis jedis = new Jedis(buildClient(clientId).get(0).getHostName(), buildClient(clientId).get(0).getPort());
		jedisByClient.put(clientId, jedis);
	}

	public Jedis get(ClientId clientId){
		return jedisByClient.get(clientId);
	}

	public List<InetSocketAddress> buildClient(ClientId clientId){
		List<InetSocketAddress> addresses;
		String clientMode = redisOptions.getClientMode(clientId.getName());
		if(clientMode.equals(RedisOptions.DYNAMIC_CLIENT_MODE)){
			addresses = Arrays.asList(redisOptions.getClusterEndpoint(clientId.getName()).get());
		}else{
			addresses = redisOptions.getServers(clientId.getName());
		}
		return addresses;
	}

}
