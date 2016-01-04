package com.hotpads.datarouter.node.type.masterslave.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface MasterSlaveMapStorageMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
extends MasterSlaveNode<PK,D,N>, MapStorage<PK,D>{

	@Override
	public default void delete(PK key, Config config) {
		getMaster().delete(key, config);
	}

	@Override
	public default void deleteAll(Config config) {
		getMaster().deleteAll(config);
	}

	@Override
	public default void deleteMulti(Collection<PK> keys, Config config) {
		getMaster().deleteMulti(keys, config);
	}

	@Override
	public default void put(D databean, Config config) {
		getMaster().put(databean, config);
	}

	@Override
	public default void putMulti(Collection<D> databeans, Config config) {
		getMaster().putMulti(databeans, config);
	}
	
	@Override
	public default boolean exists(PK key, Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : getMaster();
		return node.exists(key, config);
	}

	@Override
	public default D get(PK key, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : getMaster();
		return node.get(key, config);
	}

	@Override
	public default List<D> getMulti(Collection<PK> keys, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : getMaster();
		return node.getMulti(keys, config);
	}

	@Override
	public default List<PK> getKeys(Collection<PK> keys, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : getMaster();
		return node.getKeys(keys, config);
	}
}
