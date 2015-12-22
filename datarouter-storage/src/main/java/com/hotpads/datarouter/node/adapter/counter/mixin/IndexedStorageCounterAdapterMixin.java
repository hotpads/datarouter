package com.hotpads.datarouter.node.adapter.counter.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.CounterAdapter;
import com.hotpads.datarouter.node.op.raw.IndexedStorage;
import com.hotpads.datarouter.node.op.raw.IndexedStorage.IndexedStorageNode;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.util.core.collections.Range;

public interface IndexedStorageCounterAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedStorageNode<PK,D>>
extends IndexedStorage<PK,D>, CounterAdapter<PK,D,N>{

	//Reader

	@Override
	public default D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		String opName = IndexedStorageReader.OP_lookupUnique;
		getCounter().count(opName);
		D result = getBackingNode().lookupUnique(uniqueKey, config);
		String hitOrMiss = result != null ? "hit" : "miss";
		getCounter().count(opName + " " + hitOrMiss);
		return result;
	}

	@Override
	public default List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		String opName = IndexedStorageReader.OP_lookupMultiUnique;
		getCounter().count(opName);
		List<D> results = getBackingNode().lookupMultiUnique(uniqueKeys, config);
		int numAttempts = DrCollectionTool.size(uniqueKeys);
		int numHits = DrCollectionTool.size(results);
		int numMisses = numAttempts - numHits;
		getCounter().count(opName + " attempts", numAttempts);
		getCounter().count(opName + " hits", numHits);
		getCounter().count(opName + " misses", numMisses);
		return results;
	}

	@Override
	public default List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config){
		String opName = IndexedStorageReader.OP_lookup;
		getCounter().count(opName);
		List<D> results = getBackingNode().lookup(lookup, wildcardLastField, config);
		int numRows = DrCollectionTool.size(results);
		getCounter().count(opName + " rows", numRows);
		if(numRows == 0){
			getCounter().count(opName + " misses");
		}
		return results;
	}

	@Override
	public default List<D> lookupMulti(Collection<? extends Lookup<PK>> lookups, Config config){
		String opName = IndexedStorageReader.OP_lookupMulti;
		getCounter().count(opName);
		List<D> results = getBackingNode().lookupMulti(lookups, config);
		int numRows = DrCollectionTool.size(results);
		getCounter().count(opName + " rows", numRows);
		if(numRows == 0){
			getCounter().count(opName + " misses");
		}
		return results;
	}


	@Override
	public default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		String opName = IndexedStorageReader.OP_getFromIndex;
		getCounter().count(opName);
		List<IE> results = getBackingNode().getMultiFromIndex(keys, config, indexEntryFieldInfo);
		int numRows = DrCollectionTool.size(results);
		getCounter().count(opName + " rows", numRows);
		if(numRows == 0){
			getCounter().count(opName + " misses");
		}
		return results;
	}

	@Override
	public default <IK extends PrimaryKey<IK>, IE extends IndexEntry<IK, IE, PK, D>> List<D> getMultiByIndex(
			Collection<IK> keys, Config config){
		String opName = IndexedStorageReader.OP_getByIndex;
		getCounter().count(opName);
		List<D> results = getBackingNode().getMultiByIndex(keys, config);
		int numRows = DrCollectionTool.size(results);
		getCounter().count(opName + " rows", numRows);
		if(numRows == 0){
			getCounter().count(opName + " misses");
		}
		return results;
	}

	@Override
	public default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IE> scanIndex(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		String opName = IndexedStorageReader.OP_scanIndex;
		getCounter().count(opName);
		return getBackingNode().scanIndex(indexEntryFieldInfo, range, config);
	}

	@Override
	public default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IK> scanIndexKeys(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		String opName = IndexedStorageReader.OP_scanIndexKeys;
		getCounter().count(opName);
		return getBackingNode().scanIndexKeys(indexEntryFieldInfo, range, config);
	}

	//Writer

	@Override
	public default void delete(Lookup<PK> lookup, Config config){
		String opName = IndexedStorageWriter.OP_indexDelete;
		getCounter().count(opName);
		getBackingNode().delete(lookup, config);
	}

	@Override
	public default void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		String opName = IndexedStorageWriter.OP_deleteUnique;
		getCounter().count(opName);
		getBackingNode().deleteUnique(uniqueKey, config);
	}

	@Override
	public default void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		String opName = IndexedStorageWriter.OP_deleteMultiUnique;
		getCounter().count(opName);
		getBackingNode().deleteMultiUnique(uniqueKeys, config);
	}

	@Override
	public default <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		getCounter().count(OP_deleteByIndex);
		getBackingNode().deleteByIndex(keys, config);
	}

	@Override
	public default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<PK,D,IK,IE,IF>>
	MN registerManaged(MN managedNode){
		return getBackingNode().registerManaged(managedNode);
	}


	@Override
	public default List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return getBackingNode().getManagedNodes();
	}

}
