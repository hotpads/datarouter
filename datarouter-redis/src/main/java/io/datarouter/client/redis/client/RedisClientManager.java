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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import io.datarouter.util.timer.PhaseTimer;
import redis.clients.jedis.Jedis;

@Singleton
public class RedisClientManager extends BaseClientManager{
	private static final Logger logger = LoggerFactory.getLogger(RedisClientManager.class);

	@Inject
	private JedisHolder jedisHolder;

	@Override
	public void shutdown(ClientId clientId){
		jedisHolder.get(clientId).close();
	}

	@Override
	protected void safeInitClient(ClientId clientId){
		logger.info("activating Redis client " + clientId.getName());
		PhaseTimer timer = new PhaseTimer(clientId.getName());
		jedisHolder.registerClient(clientId);
		logger.warn(timer.add("done").toString());
	}

	public Jedis getJedis(ClientId clientId){
		initClient(clientId);
		return jedisHolder.get(clientId);
	}

}
