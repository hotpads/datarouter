package com.hotpads.datarouter.node.adapter.callsite.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.callsite.CallsiteAdapter;
import com.hotpads.datarouter.node.op.raw.IndexedStorage;
import com.hotpads.datarouter.node.op.raw.IndexedStorage.IndexedStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.collections.Range;

public interface IndexedStorageCallsiteAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedStorageNode<PK,D>>
extends IndexedStorage<PK,D>, CallsiteAdapter{

	N getBackingNode();

	@Override
	default void delete(Lookup<PK> lookup, Config config){
		config = Config.nullSafe(config).setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			getBackingNode().delete(lookup, config);
		}finally{
			recordCallsite(config, startNs, 1);
		}
	}

	@Override
	default void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		config = Config.nullSafe(config).setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			getBackingNode().deleteUnique(uniqueKey, config);
		}finally{
			recordCallsite(config, startNs, 1);
		}
	}

	@Override
	default void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		config = Config.nullSafe(config).setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			getBackingNode().deleteMultiUnique(uniqueKeys, config);
		}finally{
			recordCollectionCallsite(config, startNs, uniqueKeys);
		}
	}

	@Override
	default <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		config = Config.nullSafe(config).setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			getBackingNode().deleteByIndex(keys, config);
		}finally{
			recordCollectionCallsite(config, startNs, keys);
		}
	}

	@Override
	default D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		config = Config.nullSafe(config).setCallsite(getCallsite());
		long startNs = System.nanoTime();
		D result = null;
		try{
			result = getBackingNode().lookupUnique(uniqueKey, config);
			return result;
		}finally{
			int numResults = result == null ? 0 : 1;
			recordCallsite(config, startNs, numResults);
		}
	}

	@Override
	default List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		config = Config.nullSafe(config).setCallsite(getCallsite());
		long startNs = System.nanoTime();
		List<D> results = null;
		try{
			results = getBackingNode().lookupMultiUnique(uniqueKeys, config);
			return results;
		}finally{
			recordCollectionCallsite(config, startNs, results);
		}
	}

	@Override
	@Deprecated
	default List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config){
		config = Config.nullSafe(config).setCallsite(getCallsite());
		long startNs = System.nanoTime();
		List<D> results = null;
		try{
			results = getBackingNode().lookup(lookup, wildcardLastField, config);
			return results;
		}finally{
			recordCollectionCallsite(config, startNs, results);
		}
	}

	@Override
	@Deprecated
	default List<D> lookupMulti(Collection<? extends Lookup<PK>> lookups, Config config){
		config = Config.nullSafe(config).setCallsite(getCallsite());
		long startNs = System.nanoTime();
		List<D> results = null;
		try{
			results = getBackingNode().lookupMulti(lookups, config);
			return results;
		}finally{
			recordCollectionCallsite(config, startNs, results);
		}
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		config = Config.nullSafe(config).setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			return getBackingNode().getMultiFromIndex(keys, config, indexEntryFieldInfo);
		}finally{
			recordCollectionCallsite(config, startNs, keys);
		}
	}

	@Override
	default <IK extends PrimaryKey<IK>, IE extends IndexEntry<IK, IE, PK, D>> List<D> getMultiByIndex(
			Collection<IK> keys, Config config){
		config = Config.nullSafe(config).setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			return getBackingNode().getMultiByIndex(keys, config);
		}finally{
			recordCollectionCallsite(config, startNs, keys);
		}
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IE> scanIndex(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		config = Config.nullSafe(config).setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			return getBackingNode().scanIndex(indexEntryFieldInfo, range, config);
		}finally{
			recordCallsite(config, startNs, 1);
		}
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IK> scanIndexKeys(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		config = Config.nullSafe(config).setCallsite(getCallsite());
		long startNs = System.nanoTime();
		try{
			return getBackingNode().scanIndexKeys(indexEntryFieldInfo, range, config);
		}finally{
			recordCallsite(config, startNs, 1);
		}
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
