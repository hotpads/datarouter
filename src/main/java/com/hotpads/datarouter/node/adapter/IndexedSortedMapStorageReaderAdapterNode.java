package com.hotpads.datarouter.node.adapter;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.mixin.IndexedStorageReaderAdapterMixin;
import com.hotpads.datarouter.node.op.combo.reader.IndexedSortedMapStorageReader.IndexedSortedMapStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public class IndexedSortedMapStorageReaderAdapterNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedSortedMapStorageReaderNode<PK,D>>
extends SortedMapStorageReaderAdapterNode<PK,D,F,N>
implements IndexedSortedMapStorageReaderNode<PK,D>{
	
	private IndexedStorageReaderAdapterMixin<PK,D,F,N> indexedStorageReaderMixin;
	
	public IndexedSortedMapStorageReaderAdapterNode(NodeParams<PK,D,F> params, N backingNode){		
		super(params, backingNode);
		this.indexedStorageReaderMixin = new IndexedStorageReaderAdapterMixin<PK,D,F,N>(this, backingNode);
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
