package com.hotpads.datarouter.node.type.redundant.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.IndexedStorage;
import com.hotpads.datarouter.node.op.raw.IndexedStorage.IndexedStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.node.type.redundant.RedundantNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.collections.Range;

public interface RedundantIndexedStorageMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedStorageNode<PK,D>>
extends IndexedStorage<PK,D>, RedundantNode<PK,D,N>{

	@Override
	default void delete(Lookup<PK> lookup, Config config){
		for(N node : getWriteNodes()){
			node.delete(lookup,config);
		}
	}

	@Override
	default void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		for(N node : getWriteNodes()){
			node.deleteUnique(uniqueKey, config);
		}
	}

	@Override
	default void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		for(N node : getWriteNodes()){
			node.deleteMultiUnique(uniqueKeys, config);
		}
	}

	@Override
	default <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		for(N node : getWriteNodes()){
			node.deleteByIndex(keys, config);
		}
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<PK,D,IK,IE,IF>>
	MN registerManaged(MN managedNode){
		getWriteNodes().forEach(writeNode -> writeNode.registerManaged(managedNode));
		getReadNode().registerManaged(managedNode);
		return managedNode;
	}

	@Override
	default List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return getReadNode().getManagedNodes();
	}

	@Override
	default <IK extends PrimaryKey<IK>,IE extends IndexEntry<IK,IE,PK,D>> List<D> getMultiByIndex(Collection<IK> keys,
			Config config){
		return getReadNode().getMultiByIndex(keys, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return getReadNode().getMultiFromIndex(keys, config, indexEntryFieldInfo);
	}

	@Override
	default List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config){
		return getReadNode().lookup(lookup, wildcardLastField, config);
	}

	@Override
	default List<D> lookupMulti(Collection<? extends Lookup<PK>> lookup, Config config){
		return getReadNode().lookupMulti(lookup, config);
	}

	@Override
	default List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		return getReadNode().lookupMultiUnique(uniqueKeys, config);
	}

	@Override
	default D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		return getReadNode().lookupUnique(uniqueKey, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Iterable<IE> scanIndex(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range, Config config){
		return getReadNode().scanIndex(indexEntryFieldInfo, range, config);
	}

	@Override
	default <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Iterable<IK> scanIndexKeys(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range, Config config){
		return getReadNode().scanIndexKeys(indexEntryFieldInfo, range, config);
	}

}


