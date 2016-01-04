package com.hotpads.datarouter.node.adapter.counter;

import com.hotpads.datarouter.node.adapter.counter.mixin.MapStorageCounterAdapterMixin;
import com.hotpads.datarouter.node.adapter.counter.mixin.SortedStorageCounterAdapterMixin;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class SortedMapStorageCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedMapStorageNode<PK,D>>
extends BaseCounterAdapter<PK,D,N>
implements SortedMapStorageNode<PK,D>,
		SortedStorageCounterAdapterMixin<PK,D,N>,
		MapStorageCounterAdapterMixin<PK,D,N>{

	public SortedMapStorageCounterAdapter(N backingNode){
		super(backingNode);
	}

}
