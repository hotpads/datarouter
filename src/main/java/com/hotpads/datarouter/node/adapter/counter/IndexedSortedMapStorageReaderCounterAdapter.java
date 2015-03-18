package com.hotpads.datarouter.node.adapter.counter;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.formatter.NodeCounterFormatter;
import com.hotpads.datarouter.node.adapter.counter.mixin.IndexedStorageReaderCounterAdapterMixin;
import com.hotpads.datarouter.node.op.combo.reader.IndexedSortedMapStorageReader.IndexedSortedMapStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public class IndexedSortedMapStorageReaderCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedSortedMapStorageReaderNode<PK,D>>
extends SortedMapStorageReaderCounterAdapter<PK,D,F,N>
implements IndexedSortedMapStorageReaderNode<PK,D>{
	
	private IndexedStorageReaderCounterAdapterMixin<PK,D,F,N> indexedStorageReaderCounterAdapterMixin;
	
	public IndexedSortedMapStorageReaderCounterAdapter(N backingNode){		
		super(backingNode);
		NodeCounterFormatter<PK,D,F,N> nodeCounterFormatter = new NodeCounterFormatter<PK,D,F,N>(backingNode);
		this.indexedStorageReaderCounterAdapterMixin = new IndexedStorageReaderCounterAdapterMixin<PK,D,F,N>(
				nodeCounterFormatter, backingNode);
	}
	

	/***************** IndexedSortedMapStorageReader ************************************/

	@Override
	public Long count(Lookup<PK> lookup, Config pConfig){
		return indexedStorageReaderCounterAdapterMixin.count(lookup, pConfig);
	}

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config pConfig){
		return indexedStorageReaderCounterAdapterMixin.lookupUnique(uniqueKey, pConfig);
	}

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config pConfig){
		return indexedStorageReaderCounterAdapterMixin.lookupMultiUnique(uniqueKeys, pConfig);
	}

	@Override
	public List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config pConfig){
		return indexedStorageReaderCounterAdapterMixin.lookup(lookup, wildcardLastField, pConfig);
	}

	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookups, Config pConfig){
		return indexedStorageReaderCounterAdapterMixin.lookup(lookups, pConfig);
	}
	
}
