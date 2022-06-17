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
package io.datarouter.storage.node.adapter.counter.physical;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.PhysicalAdapterMixin;
import io.datarouter.storage.node.adapter.counter.BaseCounterAdapter;
import io.datarouter.storage.node.op.raw.TallyStorage;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;

public class PhysicalTallyStorageCounterAdapter
extends BaseCounterAdapter<TallyKey,Tally,TallyFielder,PhysicalTallyStorageNode>
implements PhysicalTallyStorageNode,
		PhysicalAdapterMixin<TallyKey,Tally,TallyFielder,PhysicalTallyStorageNode>{

	public PhysicalTallyStorageCounterAdapter(PhysicalTallyStorageNode backingNode){
		super(backingNode);
	}

	@Override
	public PhysicalDatabeanFieldInfo<TallyKey,Tally,TallyFielder> getFieldInfo(){
		return PhysicalAdapterMixin.super.getFieldInfo();
	}

	@Override
	public Long incrementAndGetCount(String key, int delta, Config config){
		getCounter().count(OP_incrementAndGetCount);
		return getBackingNode().incrementAndGetCount(key, delta, config);
	}

	@Override
	public Optional<Long> findTallyCount(String key, Config config){
		getCounter().count(OP_findTallyCount);
		return getBackingNode().findTallyCount(key, config);
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> keys, Config config){
		getCounter().count(OP_getMultiTallyCount, keys.size());
		return getBackingNode().getMultiTallyCount(keys, config);
	}

	@Override
	public void deleteTally(String key, Config config){
		getCounter().count(TallyStorage.OP_deleteTally);
		getBackingNode().deleteTally(key, config);
	}

	@Override
	public void vacuum(Config config){
		getBackingNode().vacuum(config);
	}

}
