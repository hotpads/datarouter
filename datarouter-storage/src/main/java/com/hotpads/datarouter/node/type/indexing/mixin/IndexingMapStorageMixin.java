package com.hotpads.datarouter.node.type.indexing.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.index.IndexListener;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexingMapStorageMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
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
