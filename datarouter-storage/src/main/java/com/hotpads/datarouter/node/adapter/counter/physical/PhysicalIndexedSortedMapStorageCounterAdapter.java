package com.hotpads.datarouter.node.adapter.counter.physical;

import com.hotpads.datarouter.node.adapter.PhysicalAdapterMixin;
import com.hotpads.datarouter.node.adapter.counter.BaseCounterAdapter;
import com.hotpads.datarouter.node.adapter.counter.mixin.IndexedStorageCounterAdapterMixin;
import com.hotpads.datarouter.node.adapter.counter.mixin.MapStorageCounterAdapterMixin;
import com.hotpads.datarouter.node.adapter.counter.mixin.SortedStorageCounterAdapterMixin;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class PhysicalIndexedSortedMapStorageCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalIndexedSortedMapStorageNode<PK,D>>
extends BaseCounterAdapter<PK,D,N>
implements PhysicalIndexedSortedMapStorageNode<PK,D>,
		IndexedStorageCounterAdapterMixin<PK,D,N>,
		SortedStorageCounterAdapterMixin<PK,D,N>,
		MapStorageCounterAdapterMixin<PK,D,N>,
		PhysicalAdapterMixin<PK,D,N>{

	public PhysicalIndexedSortedMapStorageCounterAdapter(N backingNode){
		super(backingNode);
	}

}
