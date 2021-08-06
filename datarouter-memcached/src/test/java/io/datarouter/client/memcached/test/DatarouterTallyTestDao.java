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
package io.datarouter.client.memcached.test;

import java.time.Duration;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

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
public class DatarouterTallyTestDao extends BaseDao implements TestDao{

	private final TallyStorage<TallyKey,Tally> node;

	@Inject
	public DatarouterTallyTestDao(Datarouter datarouter, TallyNodeFactory nodeFactory){
		super(datarouter);
		node = nodeFactory.createTally(DatarouterMemcachedTestClientIds.MEMCACHED, Tally::new, TallyFielder::new)
				.withSchemaVersion(1)
				.withTableName("Tally")
				.buildAndRegister();
	}

	public void put(Tally databean){
		node.put(databean);
	}

	public void delete(String key){
		node.deleteTally(key);
	}

	public Tally get(TallyKey key){
		return node.get(key);
	}

	public Long getTallyCount(String key){
		return node.findTallyCount(key).get();
	}

	public Optional<Long> findTallyCount(String key){
		return node.findTallyCount(key);
	}

	public Long incrementAndGetCount(String key, int delta){
		return node.incrementAndGetCount(key, delta);
	}

	public Long incrementAndGetCount(String key, int delta, Duration ttl){
		var config = new Config().setTtl(ttl);
		return node.incrementAndGetCount(key, delta, config);
	}

	public boolean exists(String key){
		return node.exists(new TallyKey(key));
	}

}
