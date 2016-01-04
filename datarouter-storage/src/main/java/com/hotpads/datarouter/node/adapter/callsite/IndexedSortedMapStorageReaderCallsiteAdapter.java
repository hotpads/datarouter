package com.hotpads.datarouter.node.adapter.callsite;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.callsite.mixin.IndexedStorageReaderCallsiteAdapterMixin;
import com.hotpads.datarouter.node.op.combo.reader.IndexedSortedMapStorageReader.IndexedSortedMapStorageReaderNode;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.collections.Range;

public class IndexedSortedMapStorageReaderCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedSortedMapStorageReaderNode<PK,D>>
extends SortedMapStorageReaderCallsiteAdapter<PK,D,F,N>
implements IndexedSortedMapStorageReaderNode<PK,D>{

	private IndexedStorageReaderCallsiteAdapterMixin<PK,D,F,N> indexedStorageReaderMixin;

	public IndexedSortedMapStorageReaderCallsiteAdapter(NodeParams<PK,D,F> params, N backingNode){
		super(params, backingNode);
		this.indexedStorageReaderMixin = new IndexedStorageReaderCallsiteAdapterMixin<>(this, backingNode);
	}

	/***************** IndexedSortedMapStorageReader ************************************/

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config config){
		return indexedStorageReaderMixin.lookupUnique(uniqueKey, config);
	}

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		return indexedStorageReaderMixin.lookupMultiUnique(uniqueKeys, config);
	}

	@Override
	public List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config config){
		return indexedStorageReaderMixin.lookup(lookup, wildcardLastField, config);
	}

	@Override
	public List<D> lookupMulti(Collection<? extends Lookup<PK>> lookups, Config config){
		return indexedStorageReaderMixin.lookupMulti(lookups, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	List<IE> getMultiFromIndex(Collection<IK> keys, Config config, DatabeanFieldInfo<IK, IE, IF> indexEntryFieldInfo){
		return indexedStorageReaderMixin.getMultiFromIndex(keys, config, indexEntryFieldInfo);
	}

	@Override
	public <IK extends PrimaryKey<IK>, IE extends IndexEntry<IK, IE, PK, D>> List<D> getMultiByIndex(
			Collection<IK> keys, Config config){
		return indexedStorageReaderMixin.getMultiByIndex(keys, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IE> scanIndex(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		return indexedStorageReaderMixin.scanIndex(indexEntryFieldInfo, range, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK, IE, PK, D>,
			IF extends DatabeanFielder<IK, IE>>
	Iterable<IK> scanIndexKeys(DatabeanFieldInfo<IK,IE,IF> indexEntryFieldInfo, Range<IK> range,
			Config config){
		return indexedStorageReaderMixin.scanIndexKeys(indexEntryFieldInfo, range, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>,
			IE extends IndexEntry<IK,IE,PK,D>,
			IF extends DatabeanFielder<IK,IE>,
			MN extends ManagedNode<PK,D,IK,IE,IF>>
	MN registerManaged(MN managedNode){
		return indexedStorageReaderMixin.registerManaged(managedNode);
	}

	@Override
	public List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return indexedStorageReaderMixin.getManagedNodes();
	}
}
