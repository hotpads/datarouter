package com.hotpads.datarouter.node.adapter.counter.physical;

import com.hotpads.datarouter.node.adapter.PhysicalAdapterMixin;
import com.hotpads.datarouter.node.adapter.counter.BaseCounterAdapter;
import com.hotpads.datarouter.node.adapter.counter.mixin.MapStorageCounterAdapterMixin;
import com.hotpads.datarouter.node.adapter.counter.mixin.SortedStorageCounterAdapterMixin;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class PhysicalSortedMapStorageCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalSortedMapStorageNode<PK,D>>
extends BaseCounterAdapter<PK,D,N>
implements PhysicalSortedMapStorageNode<PK,D>,
		SortedStorageCounterAdapterMixin<PK,D,N>,
		MapStorageCounterAdapterMixin<PK,D,N>,
		PhysicalAdapterMixin<PK,D,N>{


	public PhysicalSortedMapStorageCounterAdapter(N backingNode){
		super(backingNode);
	}

}
