package com.hotpads.datarouter.node.type.writebehind;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.IndexedSortedMapStorageReader;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.collections.Range;

public class WriteBehindIndexedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedSortedMapStorageReader<PK,D>>
extends WriteBehindSortedMapStorageReaderNode<PK,D,N>
implements IndexedSortedMapStorageReader<PK,D>{

	public WriteBehindIndexedMapStorageReaderNode(Datarouter datarouter, N backingNode){
		super(datarouter, backingNode);
	}

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		return backingNode.lookupUnique(uniqueKey, config);
	}

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		return backingNode.lookupMultiUnique(uniqueKeys, config);
	}

	@Override
	public List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config){
		return backingNode.lookup(lookup, wildcardLastField, config);
	}

	@Override
	public List<D> lookupMulti(Collection<? extends Lookup<PK>> lookup, Config config){
		return backingNode.lookupMulti(lookup, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo){
		return backingNode.getMultiFromIndex(keys, config, indexEntryFieldInfo);
	}

	@Override
	public <IK extends PrimaryKey<IK>, IE extends IndexEntry<IK,IE,PK,D>> List<D> getMultiByIndex(
			Collection<IK> keys, Config config){
		return backingNode.getMultiByIndex(keys, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Iterable<IE> scanIndex(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		return backingNode.scanIndex(indexEntryFieldInfo, range, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>>
	Iterable<IK> scanIndexKeys(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		return backingNode.scanIndexKeys(indexEntryFieldInfo, range, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<PK,D,IK,IE,IF>>
	MN registerManaged(MN managedNode){
		return backingNode.registerManaged(managedNode);
	}

	@Override
	public List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return backingNode.getManagedNodes();
	}

}
