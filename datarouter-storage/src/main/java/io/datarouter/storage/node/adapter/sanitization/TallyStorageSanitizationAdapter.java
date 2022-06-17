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
package io.datarouter.storage.node.adapter.sanitization;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.TallyStorage;
import io.datarouter.storage.node.op.raw.TallyStorage.PhysicalTallyStorageNode;
import io.datarouter.storage.tally.Tally;
import io.datarouter.storage.tally.Tally.TallyFielder;
import io.datarouter.storage.tally.TallyKey;

public class TallyStorageSanitizationAdapter
extends BaseSanitizationAdapter<TallyKey,Tally,TallyFielder,PhysicalTallyStorageNode>
implements TallyStorage{

	public TallyStorageSanitizationAdapter(PhysicalTallyStorageNode backingNode){
		super(backingNode);
	}

	@Override
	public Long incrementAndGetCount(String key, int delta, Config config){
		Objects.requireNonNull(key);
		Objects.requireNonNull(config);
		return backingNode.incrementAndGetCount(key, delta, config);
	}

	@Override
	public Optional<Long> findTallyCount(String key, Config config){
		Objects.requireNonNull(key);
		Objects.requireNonNull(config);
		return backingNode.findTallyCount(key, config);
	}

	@Override
	public Map<String,Long> getMultiTallyCount(Collection<String> keys, Config config){
		Objects.requireNonNull(keys);
		Objects.requireNonNull(config);
		keys.forEach(Objects::requireNonNull);
		if(keys.isEmpty()){
			return Map.of();
		}
		return backingNode.getMultiTallyCount(keys, config);
	}

	@Override
	public void deleteTally(String key, Config config){
		Objects.requireNonNull(key);
		Objects.requireNonNull(config);
		backingNode.deleteTally(key, config);
	}

	@Override
	public void vacuum(Config config){
		Objects.requireNonNull(config);
		backingNode.vacuum(config);
	}

}
