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
package io.datarouter.ratelimiter.storage;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.node.factory.TallyNodeFactory;
import io.datarouter.storage.node.op.raw.TallyStorage;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;

public abstract class BaseTallyDao extends BaseDao{

	private final TallyStorage<TallyKey,Tally> node;

	public BaseTallyDao(Datarouter datarouter, TallyNodeFactory nodeFactory, ClientId clientId, int version){
		super(datarouter);
		node = nodeFactory.createTally(clientId, Tally::new, TallyFielder::new)
				.withSchemaVersion(version)
				.withTableName("Tally")
				.withIsSystemTable(true)
				.buildAndRegister();
	}

	public Long incrementAndGetCount(String key, int delta, Duration ttl, Duration timeout){
		var config = new Config()
				.setTtl(ttl)
				.setTimeout(timeout);
		return node.incrementAndGetCount(key, delta, config);
	}

	public Map<String,Long> getMultiTallyCount(Collection<String> keys, Duration ttl, Duration timeout){
		var config = new Config()
				.setTtl(ttl)
				.setTimeout(timeout);
		return node.getMultiTallyCount(keys, config);
	}

}
