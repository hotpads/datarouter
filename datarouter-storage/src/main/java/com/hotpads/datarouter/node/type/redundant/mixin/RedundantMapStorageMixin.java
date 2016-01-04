package com.hotpads.datarouter.node.type.redundant.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.redundant.RedundantNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface RedundantMapStorageMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
extends MapStorage<PK,D>, RedundantNode<PK,D,N>{

	@Override
	default void delete(PK key, Config config) {
		for(N n : getWriteNodes()){
			n.delete(key, config);
		}
	}

	@Override
	default void deleteAll(Config config) {
		for(N n : getWriteNodes()){
			n.deleteAll(config);
		}
	}

	@Override
	default void deleteMulti(Collection<PK> keys, Config config) {
		for(N n : getWriteNodes()){
			n.deleteMulti(keys, config);
		}
	}

	@Override
	default void put(D databean, Config config) {
		for(N n : getWriteNodes()){
			n.put(databean, config);
		}
	}

	@Override
	default void putMulti(Collection<D> databeans, Config config) {
		for(N n : getWriteNodes()){
			n.putMulti(databeans, config);
		}
	}

	@Override
	default boolean exists(PK key, Config config){
		return getReadNode().exists(key, config);
	}

	@Override
	default D get(PK key, Config config) {
		return getReadNode().get(key, config);
	}

	@Override
	default List<D> getMulti(Collection<PK> keys, Config config) {
		return getReadNode().getMulti(keys, config);
	}

	@Override
	default List<PK> getKeys(Collection<PK> keys, Config config) {
		return getReadNode().getKeys(keys, config);
	}

}
