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
package io.datarouter.virtualnode.caching;

import java.util.Objects;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.adapter.callsite.MapStorageCallsiteAdapter;
import io.datarouter.storage.node.adapter.counter.MapStorageCounterAdapter;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import jakarta.inject.Singleton;

@Singleton
public class CachingNodeFactory{

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends MapStorageNode<PK,D,F>>
	MapStorageNode<PK,D,F> create(
			N cacheNode,
			N backingNode,
			boolean cacheReads,
			boolean cacheWrites,
			boolean addAdapter){
		MapStorageNode<PK,D,F> node = new MapCachingMapStorageNode<>(cacheNode, backingNode, cacheReads, cacheWrites);
		node = new MapStorageCounterAdapter<>(node);
		if(addAdapter){
			node = new MapStorageCallsiteAdapter<>(node);
		}
		return Objects.requireNonNull(node, "cannot build caching Node");
	}

}
