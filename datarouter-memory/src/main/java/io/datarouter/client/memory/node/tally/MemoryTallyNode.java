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
package io.datarouter.client.memory.node.tally;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import io.datarouter.client.memory.MemoryClientType;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;

public class MemoryTallyNode
extends BasePhysicalNode<TallyKey,Tally,TallyFielder>
implements PhysicalTallyStorageNode{

	private final MemoryTallyStorage storage;

	public MemoryTallyNode(
			NodeParams<TallyKey,Tally,TallyFielder> params,
			MemoryClientType memoryClientType){
		super(params, memoryClientType);
		storage = new MemoryTallyStorage();
	}

	/*------------- TallyStorage -------------*/

	@Override
	public Long incrementAndGetCount(String id, int delta, Config config){
		Long ttlMs = config.findTtl()
				.map(Duration::toMillis)
				.orElse(null);
		return storage.addAndGet(id, delta, ttlMs);
	}

	@Override
	public Optional<Long> findTallyCount(String id, Config config){
		return Optional.ofNullable(storage.get(id))
				.filter(MemoryTally::notExpired)
				.map(MemoryTally::getTally);
	}

	/**
	 * Non-atomic between ids to match distributed backends like memcached
	 */
	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> ids, Config config){
		return Scanner.of(ids)
				.map(id -> new Object(){
					String tallyId = id;
					MemoryTally tallyValue = storage.get(id);
				})
				.exclude(obj -> obj.tallyValue == null)
				.exclude(obj -> obj.tallyValue.isExpired())
				.toMap(obj -> obj.tallyId, obj -> obj.tallyValue.getTally());
	}

	@Override
	public void deleteTally(String id, Config config){
		storage.delete(id);
	}

	@Override
	public void vacuum(Config config){
		throw new UnsupportedOperationException();
	}

}
