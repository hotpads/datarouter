package com.hotpads.datarouter.node.adapter.availability.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.exception.UnavailableException;
import com.hotpads.datarouter.node.op.raw.IndexedStorage;
import com.hotpads.datarouter.node.op.raw.IndexedStorage.PhysicalIndexedStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.collections.Range;

public interface PhysicalIndexedStorageAvailabilityAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalIndexedStorageNode<PK,D>>
extends IndexedStorage<PK,D>{

	N getBackingNode();
	UnavailableException makeUnavailableException();

	@Override
	default D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		if(getBackingNode().getClient().isAvailable()){
			return getBackingNode().lookupUnique(uniqueKey, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		if(getBackingNode().getClient().isAvailable()){
			return getBackingNode().lookupMultiUnique(uniqueKeys, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config){
		if(getBackingNode().getClient().isAvailable()){
			return getBackingNode().lookup(lookup, wildcardLastField, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default List<D> lookupMulti(Collection<? extends Lookup<PK>> lookups, Config config){
		if(getBackingNode().getClient().isAvailable()){
			return getBackingNode().lookupMulti(lookups, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		if(getBackingNode().getClient().isAvailable()){
			return getBackingNode().getMultiFromIndex(keys, config, indexEntryFieldInfo);
		}
		throw makeUnavailableException();
	}

	@Override
	default <IK extends PrimaryKey<IK>, IE extends IndexEntry<IK, IE, PK, D>> List<D> getMultiByIndex(
			Collection<IK> keys, Config config){
		if(getBackingNode().getClient().isAvailable()){
			return getBackingNode().getMultiByIndex(keys, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IE> scanIndex(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		if(getBackingNode().getClient().isAvailable()){
			return getBackingNode().scanIndex(indexEntryFieldInfo, range, config);
		}
		throw makeUnavailableException();
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IK> scanIndexKeys(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		if(getBackingNode().getClient().isAvailable()){
			return getBackingNode().scanIndexKeys(indexEntryFieldInfo, range, config);
		}
		throw makeUnavailableException();
	}

	//Writer

	@Override
	default void delete(Lookup<PK> lookup, Config config){
		if(getBackingNode().getClient().isAvailable()){
			getBackingNode().delete(lookup, config);
			return;
		}
		throw makeUnavailableException();
	}

	@Override
	default void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		if(getBackingNode().getClient().isAvailable()){
			getBackingNode().deleteUnique(uniqueKey, config);
			return;
		}
		throw makeUnavailableException();
	}

	@Override
	default void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		if(getBackingNode().getClient().isAvailable()){
			getBackingNode().deleteMultiUnique(uniqueKeys, config);
			return;
		}
		throw makeUnavailableException();
	}

	@Override
	default <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		if(getBackingNode().getClient().isAvailable()){
			getBackingNode().deleteByIndex(keys, config);
			return;
		}
		throw makeUnavailableException();
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<PK,D,IK,IE,IF>>
	MN registerManaged(MN managedNode){
		return getBackingNode().registerManaged(managedNode);
	}


	@Override
	default List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return getBackingNode().getManagedNodes();
	}

}
