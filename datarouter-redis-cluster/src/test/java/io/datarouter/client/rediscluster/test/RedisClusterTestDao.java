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
package io.datarouter.client.rediscluster.test;

import java.time.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.rediscluster.RedisClusterTestClientIds;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.TallyNodeFactory;
import io.datarouter.storage.node.op.raw.TallyStorage;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;

@Singleton
public class RedisClusterTestDao extends BaseDao implements TestDao{

	private final TallyStorage<TallyKey,Tally> node;

	@Inject
	public RedisClusterTestDao(Datarouter datarouter, TallyNodeFactory nodeFactory){
		super(datarouter);
		node = nodeFactory.createTally(RedisClusterTestClientIds.REDIS_CLUSTER,Tally::new, TallyFielder::new)
				.withSchemaVersion(1)
				.withTableName("TallyTtlTest")
				.buildAndRegister();
	}

	public void delete(TallyKey key){
		node.delete(key);
	}

	public boolean exists(TallyKey key){
		return node.exists(key);
	}

	public void increment(TallyKey key, int delta, Duration ttl){
		node.incrementAndGetCount(key.getId(), delta, new Config().setTtl(ttl));
	}

	public void increment(TallyKey key, int delta){
		node.incrementAndGetCount(key.getId(), delta);
	}

}
