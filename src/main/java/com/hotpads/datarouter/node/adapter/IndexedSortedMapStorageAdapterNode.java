package com.hotpads.datarouter.node.adapter;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.mixin.IndexedStorageWriterAdapterMixin;
import com.hotpads.datarouter.node.adapter.mixin.MapStorageWriterAdapterMixin;
import com.hotpads.datarouter.node.adapter.mixin.SortedStorageWriterAdapterMixin;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public class IndexedSortedMapStorageAdapterNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedSortedMapStorageNode<PK,D>>
extends IndexedSortedMapStorageReaderAdapterNode<PK,D,F,N>
implements IndexedSortedMapStorageNode<PK,D>{

	private MapStorageWriterAdapterMixin<PK,D,F,N> mapStorageWriterMixin;
	private SortedStorageWriterAdapterMixin<PK,D,F,N> sortedStorageWriterMixin;
	private IndexedStorageWriterAdapterMixin<PK,D,F,N> indexedStorageWriterMixin;
	
	
	public IndexedSortedMapStorageAdapterNode(NodeParams<PK,D,F> params, N backingNode){		
		super(params, backingNode);
		this.mapStorageWriterMixin = new MapStorageWriterAdapterMixin<PK,D,F,N>(this, backingNode);
		this.sortedStorageWriterMixin = new SortedStorageWriterAdapterMixin<PK,D,F,N>(this, backingNode);
		this.indexedStorageWriterMixin = new IndexedStorageWriterAdapterMixin<PK,D,F,N>(this, backingNode);
	}
	

	/***************** MapStorage ************************************/
	
	@Override
	public void put(D databean, Config pConfig){
		mapStorageWriterMixin.put(databean, pConfig);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config pConfig){
		mapStorageWriterMixin.putMulti(databeans, pConfig);
	}

	@Override
	public void delete(PK key, Config pConfig){
		mapStorageWriterMixin.delete(key, pConfig);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config pConfig){
		mapStorageWriterMixin.deleteMulti(keys, pConfig);
	}

	@Override
	public void deleteAll(Config pConfig){
		mapStorageWriterMixin.deleteAll(pConfig);
	}
	

	/***************** SortedMapStorage ************************************/

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config pConfig){
		sortedStorageWriterMixin.deleteRangeWithPrefix(prefix, wildcardLastField, pConfig);
	}
	

	/***************** IndexedSortedMapStorage ************************************/

	@Override
	public void delete(Lookup<PK> lookup, Config pConfig){
		indexedStorageWriterMixin.delete(lookup, pConfig);
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config pConfig){
		indexedStorageWriterMixin.deleteUnique(uniqueKey, pConfig);
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config pConfig){
		indexedStorageWriterMixin.deleteMultiUnique(uniqueKeys, pConfig);
	}

	
}
