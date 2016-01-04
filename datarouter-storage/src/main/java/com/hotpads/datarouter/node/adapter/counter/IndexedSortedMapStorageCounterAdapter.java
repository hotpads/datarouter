package com.hotpads.datarouter.node.adapter.counter;

import com.hotpads.datarouter.node.adapter.counter.mixin.IndexedStorageCounterAdapterMixin;
import com.hotpads.datarouter.node.adapter.counter.mixin.MapStorageCounterAdapterMixin;
import com.hotpads.datarouter.node.adapter.counter.mixin.SortedStorageCounterAdapterMixin;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class IndexedSortedMapStorageCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends IndexedSortedMapStorageNode<PK,D>>
extends BaseCounterAdapter<PK,D,N>
implements IndexedSortedMapStorageNode<PK,D>,
		MapStorageCounterAdapterMixin<PK,D,N>,
		SortedStorageCounterAdapterMixin<PK,D,N>,
		IndexedStorageCounterAdapterMixin<PK,D,N>{

	public IndexedSortedMapStorageCounterAdapter(N backingNode){
		super(backingNode);
	}

}
