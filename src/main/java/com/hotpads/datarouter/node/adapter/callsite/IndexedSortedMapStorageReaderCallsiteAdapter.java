package com.hotpads.datarouter.node.adapter.callsite;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.callsite.mixin.IndexedStorageReaderCallsiteAdapterMixin;
import com.hotpads.datarouter.node.op.combo.reader.IndexedSortedMapStorageReader.IndexedSortedMapStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

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
		this.indexedStorageReaderMixin = new IndexedStorageReaderCallsiteAdapterMixin<PK,D,F,N>(this, backingNode);
	}
	

	/***************** IndexedSortedMapStorageReader ************************************/

	@Override
	public Long count(Lookup<PK> lookup, Config pConfig){
		return indexedStorageReaderMixin.count(lookup, pConfig);
	}

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config pConfig){
		return indexedStorageReaderMixin.lookupUnique(uniqueKey, pConfig);
	}

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config pConfig){
		return indexedStorageReaderMixin.lookupMultiUnique(uniqueKeys, pConfig);
	}

	@Override
	public List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config pConfig){
		return indexedStorageReaderMixin.lookup(lookup, wildcardLastField, pConfig);
	}

	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookups, Config pConfig){
		return indexedStorageReaderMixin.lookup(lookups, pConfig);
	}
	
}
