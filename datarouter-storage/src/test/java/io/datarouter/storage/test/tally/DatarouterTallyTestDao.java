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
package io.datarouter.storage.test.tally;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.inject.Singleton;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.TallyNodeFactory;
import io.datarouter.storage.node.op.raw.TallyStorage;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;

@Singleton
public class DatarouterTallyTestDao extends BaseDao implements TestDao{

	private final TallyStorage node;

	public DatarouterTallyTestDao(Datarouter datarouter, TallyNodeFactory nodeFactory, ClientId clientId){
		super(datarouter);
		node = nodeFactory.createTally(clientId, Tally::new, TallyFielder::new)
				.withSchemaVersion("1")
				.withTableName("Tally")
				.buildAndRegister();
	}

	public void deleteTally(String key){
		node.deleteTally(key);
	}

	public Long getTallyCount(String key){
		return node.findTallyCount(key).orElseThrow();
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

	public void vacuum(Config config){
		node.vacuum(config);
	}

	public Map<String,Long> getMulti(Collection<String> keys){
		return node.getMultiTallyCount(keys);
	}

}
