package com.hotpads.datarouter.node.adapter.counter;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.formatter.NodeCounterFormatter;
import com.hotpads.datarouter.node.adapter.counter.mixin.IndexedStorageWriterCounterAdapterMixin;
import com.hotpads.datarouter.node.adapter.counter.mixin.MapStorageWriterCounterAdapterMixin;
import com.hotpads.datarouter.node.adapter.counter.mixin.SortedStorageWriterCounterAdapterMixin;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public class IndexedSortedMapStorageCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedSortedMapStorageNode<PK,D>>
extends IndexedSortedMapStorageReaderCounterAdapter<PK,D,F,N>
implements IndexedSortedMapStorageNode<PK,D>{

	private MapStorageWriterCounterAdapterMixin<PK,D,F,N> mapStorageWriterMixin;
	private SortedStorageWriterCounterAdapterMixin<PK,D,F,N> sortedStorageWriterMixin;
	private IndexedStorageWriterCounterAdapterMixin<PK,D,F,N> indexedStorageWriterMixin;
	
	
	public IndexedSortedMapStorageCounterAdapter(N backingNode){		
		super(backingNode);
		NodeCounterFormatter<PK,D,F,N> nodeCounterFormatter = new NodeCounterFormatter<PK,D,F,N>(backingNode);
		this.mapStorageWriterMixin = new MapStorageWriterCounterAdapterMixin<PK,D,F,N>(nodeCounterFormatter, 
				backingNode);
		this.sortedStorageWriterMixin = new SortedStorageWriterCounterAdapterMixin<PK,D,F,N>(nodeCounterFormatter, 
				backingNode);
		this.indexedStorageWriterMixin = new IndexedStorageWriterCounterAdapterMixin<PK,D,F,N>(nodeCounterFormatter, 
				backingNode);
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
