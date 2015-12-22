package com.hotpads.datarouter.node.adapter.availability.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.UnavailableException;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface PhysicalMapStorageAvailabilityAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalMapStorageNode<PK,D>>
extends MapStorage<PK,D>{

	N getBackingNode();
	UnavailableException makeUnavailableException();

	@Override
	default boolean exists(PK key, Config config){
		if(getBackingNode().getClient().isAvailable()){
			return getBackingNode().exists(key, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default List<PK> getKeys(Collection<PK> keys, Config config){
		if(getBackingNode().getClient().isAvailable()){
			return getBackingNode().getKeys(keys, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default D get(PK key, Config config){
		if(getBackingNode().getClient().isAvailable()){
			return getBackingNode().get(key, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default List<D> getMulti(Collection<PK> keys, Config config){
		if(getBackingNode().getClient().isAvailable()){
			return getBackingNode().getMulti(keys, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default void delete(PK key, Config config){
		if(getBackingNode().getClient().isAvailable()){
			getBackingNode().delete(key, config);
			return;
		}
		throw makeUnavailableException();
	}

	@Override
	default void deleteMulti(Collection<PK> keys, Config config){
		if(getBackingNode().getClient().isAvailable()){
			getBackingNode().deleteMulti(keys, config);
			return;
		}
		throw makeUnavailableException();
	}

	@Override
	default void deleteAll(Config config){
		if(getBackingNode().getClient().isAvailable()){
			getBackingNode().deleteAll(config);
			return;
		}
		throw makeUnavailableException();
	}

	@Override
	default void put(D databean, Config config){
		if(getBackingNode().getClient().isAvailable()){
			getBackingNode().put(databean, config);
			return;
		}
		throw makeUnavailableException();
	}

	@Override
	default void putMulti(Collection<D> databeans, Config config){
		if(getBackingNode().getClient().isAvailable()){
			getBackingNode().putMulti(databeans, config);
			return;
		}
		throw makeUnavailableException();
	}

}
