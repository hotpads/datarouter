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
package io.datarouter.storage.node.adapter.counter.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.counter.CounterAdapter;
import io.datarouter.storage.node.op.raw.read.MapStorageReader;
import io.datarouter.storage.node.op.raw.read.MapStorageReader.MapStorageReaderNode;

public interface MapStorageReaderCounterAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageReaderNode<PK,D,F>>
extends MapStorageReader<PK,D>, CounterAdapter<PK,D,F,N>{

	@Override
	public default boolean exists(PK key, Config config){
		String opName = MapStorageReader.OP_exists;
		getCounter().count(opName);
		boolean result = getBackingNode().exists(key, config);
		String hitOrMiss = result ? "hit" : "miss";
		getCounter().count(opName + " " + hitOrMiss);
		return result;
	}

	@Override
	public default D get(PK key, Config config){
		String opName = MapStorageReader.OP_get;
		getCounter().count(opName);
		D result = getBackingNode().get(key, config);
		String hitOrMiss = result != null ? "hit" : "miss";
		getCounter().count(opName + " " + hitOrMiss);
		return result;
	}

	@Override
	public default List<D> getMulti(Collection<PK> keys, Config config){
		if(keys == null || keys.isEmpty()){
			return new ArrayList<>();
		}
		String opName = MapStorageReader.OP_getMulti;
		getCounter().count(opName);
		getCounter().count(opName + " keys", keys.size());
		List<D> results = getBackingNode().getMulti(keys, config);
		int numHits = results.size();
		int numMisses = keys.size() - numHits;
		getCounter().count(opName + " hit", numHits);
		getCounter().count(opName + " miss", numMisses);
		return results;
	}

	@Override
	public default List<PK> getKeys(Collection<PK> keys, Config config){
		if(keys == null || keys.isEmpty()){
			return new ArrayList<>();
		}
		String opName = MapStorageReader.OP_getKeys;
		getCounter().count(opName);
		getCounter().count(opName + " keys", keys.size());
		List<PK> results = getBackingNode().getKeys(keys, config);
		int numHits = results.size();
		int numMisses = keys.size() - numHits;
		getCounter().count(opName + " hit", numHits);
		getCounter().count(opName + " miss", numMisses);
		return results;
	}

}
