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
package io.datarouter.client.redis.test;

import java.time.Duration;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.redis.RedisTestClientIds;
import io.datarouter.client.redis.RedisClientType;
import io.datarouter.client.redis.databean.RedisDatabean;
import io.datarouter.client.redis.databean.RedisDatabeanKey;
import io.datarouter.client.redis.databean.RedisDatabean.RedisDatabeanFielder;
import io.datarouter.client.redis.node.RedisNode;
import io.datarouter.inject.DatarouterInjector;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.DatarouterClients;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;

@Singleton
public class RedisTestDao extends BaseDao implements TestDao{

	private final RedisNode<RedisDatabeanKey,RedisDatabean,RedisDatabeanFielder> node;

	@Inject
	public RedisTestDao(Datarouter datarouter, DatarouterClients datarouterClients, DatarouterInjector injector){
		super(datarouter);
		RedisClientType clientType = (RedisClientType)datarouterClients.getClientTypeInstance(
				RedisTestClientIds.REDIS);
		Objects.requireNonNull(clientType, "clientType not found for clientName:" + RedisTestClientIds.REDIS
				.getName());
		NodeParams<RedisDatabeanKey,RedisDatabean,RedisDatabeanFielder> params = new NodeParamsBuilder<>(
				RedisDatabean::new, RedisDatabeanFielder::new)
				.withClientId(RedisTestClientIds.REDIS)
				.withSchemaVersion(1)
				.build();
		this.node = datarouter.register(injector.getInstance(clientType.getClientNodeFactoryClass())
				.createNodeWithoutAdapters(params));
	}

	public void delete(RedisDatabeanKey key){
		node.delete(key);
	}

	public boolean exists(RedisDatabeanKey key){
		return node.exists(key);
	}

	public void increment(RedisDatabeanKey key, int delta, Duration ttl){
		node.increment(key, delta, new Config().setTtl(ttl));
	}

	public void increment(RedisDatabeanKey key, int delta){
		node.increment(key, delta);
	}

}
