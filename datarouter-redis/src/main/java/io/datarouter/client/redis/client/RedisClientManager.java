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

import io.datarouter.storage.client.BaseClientManager;
import io.datarouter.storage.client.ClientId;
import io.lettuce.core.api.StatefulRedisConnection;

@Singleton
public class RedisClientManager extends BaseClientManager{

	@Inject
	private RedisClientHolder holder;

	@Override
	public void shutdown(ClientId clientId){
		holder.get(clientId).close();
	}

	@Override
	protected void safeInitClient(ClientId clientId){
		holder.registerClient(clientId);
	}

	public StatefulRedisConnection<String,String> getClient(ClientId clientId){
		initClient(clientId);
		return holder.get(clientId);
	}

}
