package com.hotpads.datarouter.node.type.masterslave.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.IndexedStorage;
import com.hotpads.datarouter.node.op.raw.IndexedStorage.IndexedStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.node.type.masterslave.MasterSlaveNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.collections.Range;

public interface MasterSlaveIndexedStorageMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedStorageNode<PK,D>>
extends MasterSlaveNode<PK,D,N>, IndexedStorage<PK,D>{

	@Override
	public default D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : getMaster();
		return node.lookupUnique(uniqueKey, config);
	}


	@Override
	public default List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : getMaster();
		return node.lookupMultiUnique(uniqueKeys, config);
	}


	@Override
	public default List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : getMaster();
		return node.lookup(lookup, wildcardLastField, config);
	}


	@Override
	public default List<D> lookupMulti(Collection<? extends Lookup<PK>> lookups, Config config) {
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : getMaster();
		return node.lookupMulti(lookups, config);
	}

	@Override
	public default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : getMaster();
		return node.getMultiFromIndex(keys, config, indexEntryFieldInfo);
	}

	@Override
	public default <IK extends PrimaryKey<IK>, IE extends IndexEntry<IK, IE, PK, D>> List<D> getMultiByIndex(
			Collection<IK> keys, Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : getMaster();
		return node.getMultiByIndex(keys, config);
	}

	@Override
	public default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IE> scanIndex(DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : getMaster();
		return node.scanIndex(indexEntryFieldInfo, range, config);
	}

	@Override
	public default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IK> scanIndexKeys(DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		boolean slaveOk = Config.nullSafe(config).getSlaveOk();
		N node = slaveOk ? chooseSlave(config) : getMaster();
		return node.scanIndexKeys(indexEntryFieldInfo, range, config);
	}

	@Override
	public default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<PK,D,IK,IE,IF>>
	MN registerManaged(MN managedNode){
		for(N node : getChildNodes()){
			node.registerManaged(managedNode);
		}
		return managedNode;
	}

	@Override
	public default List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return getMaster().getManagedNodes();
	}

	@Override
	public default void delete(Lookup<PK> lookup, Config config) {
		getMaster().delete(lookup, config);
	}

	@Override
	public default void deleteUnique(UniqueKey<PK> uniqueKey, Config config) {
		getMaster().deleteUnique(uniqueKey, config);
	}

	@Override
	public default void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config) {
		getMaster().deleteMultiUnique(uniqueKeys, config);
	}

	@Override
	public default <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		getMaster().deleteByIndex(keys, config);
	}

}
