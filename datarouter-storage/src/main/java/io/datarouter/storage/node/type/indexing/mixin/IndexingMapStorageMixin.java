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
package io.datarouter.storage.node.type.indexing.mixin;

import java.util.Collection;
import java.util.List;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.storage.node.op.raw.index.IndexListener;

public interface IndexingMapStorageMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageNode<PK,D,F>>
extends MapStorage<PK,D>{

	N getBackingNode();
	List<IndexListener<PK,D>> getIndexNodes();

	@Override
	default void delete(PK key, Config config){
		for(IndexListener<PK,D> indexNode : getIndexNodes()){
			indexNode.onDelete(key, config);
		}
		getBackingNode().delete(key, config);
	}

	//this method is not used right now but added it for completion
	default void deleteDatabean(D databean, Config config){
		for(IndexListener<PK,D> indexNode : getIndexNodes()){
			indexNode.onDeleteDatabean(databean, config);
		}
		getBackingNode().delete(databean.getKey(), config);
	}

	@Override
	default void deleteAll(Config config){
		for(IndexListener<PK,D> indexNode : getIndexNodes()){
			indexNode.onDeleteAll(config);
		}
		getBackingNode().deleteAll(config);
	}

	@Override
	default void deleteMulti(Collection<PK> keys, Config config){
		for(IndexListener<PK,D> indexNode : getIndexNodes()){
			indexNode.onDeleteMulti(keys, config);
		}
		getBackingNode().deleteMulti(keys, config);
	}

	default void deleteMultiDatabeans(Collection<D> databeans, Config config){
		for(IndexListener<PK,D> indexNode : getIndexNodes()){
			indexNode.onDeleteMultiDatabeans(databeans, config);
		}
		getBackingNode().deleteMulti(DatabeanTool.getKeys(databeans), config);
	}

	@Override
	default void put(D databean, Config config){
		for(IndexListener<PK,D> indexNode : getIndexNodes()){
			indexNode.onPut(databean, config);
		}
		getBackingNode().put(databean, config);
	}

	@Override
	default void putMulti(Collection<D> databeans, Config config){
		for(IndexListener<PK,D> indexNode : getIndexNodes()){
			indexNode.onPutMulti(databeans, config);
		}
		getBackingNode().putMulti(databeans, config);
	}

	@Override
	default boolean exists(PK key, Config config){
		return getBackingNode().exists(key, config);
	}

	@Override
	default D get(PK key, Config config){
		return getBackingNode().get(key, config);
	}

	@Override
	default List<D> getMulti(Collection<PK> keys, Config config){
		return getBackingNode().getMulti(keys, config);
	}

	@Override
	default List<PK> getKeys(Collection<PK> keys, Config config){
		return getBackingNode().getKeys(keys, config);
	}

}
