package com.hotpads.datarouter.node.adapter.counter.physical;

import com.hotpads.datarouter.node.adapter.PhysicalAdapterMixin;
import com.hotpads.datarouter.node.adapter.counter.BaseCounterAdapter;
import com.hotpads.datarouter.node.adapter.counter.mixin.MapStorageCounterAdapterMixin;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class PhysicalMapStorageCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalMapStorageNode<PK,D>>
extends BaseCounterAdapter<PK,D,N>
implements PhysicalMapStorageNode<PK,D>,
		MapStorageCounterAdapterMixin<PK,D,N>,
		PhysicalAdapterMixin<PK,D,N>{

	public PhysicalMapStorageCounterAdapter(N backingNode){
		super(backingNode);
	}

}
