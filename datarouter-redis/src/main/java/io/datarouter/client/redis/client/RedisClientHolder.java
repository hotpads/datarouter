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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.datarouter.storage.client.ClientId;
import jakarta.inject.Singleton;

@Singleton
public class RedisClientHolder{

	private final Map<ClientId,DatarouterRedisClient> redisByClientId;

	public RedisClientHolder(){
		redisByClientId = new ConcurrentHashMap<>();
	}

	public void registerClient(ClientId clientId, DatarouterRedisClient client){
		if(redisByClientId.containsKey(clientId)){
			throw new RuntimeException(clientId + " already registered a RedisClient");
		}
		redisByClientId.put(clientId, client);
	}

	public DatarouterRedisClient get(ClientId clientId){
		return redisByClientId.get(clientId);
	}

}
