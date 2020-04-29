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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import redis.clients.jedis.JedisCluster;

@Singleton
public class RedisClusterClientManager extends BaseClientManager{

	@Inject
	private JedisClusterHolder jedisHolder;

	@Override
	public void shutdown(ClientId clientId){
		jedisHolder.get(clientId).close();
	}

	@Override
	protected void safeInitClient(ClientId clientId){
		jedisHolder.registerClient(clientId);
	}

	public JedisCluster getJedis(ClientId clientId){
		initClient(clientId);
		return jedisHolder.get(clientId);
	}

}
