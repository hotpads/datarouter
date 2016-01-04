package com.hotpads.datarouter.node.adapter.counter;

import com.hotpads.datarouter.node.adapter.counter.mixin.MapStorageCounterAdapterMixin;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class MapStorageCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends MapStorageNode<PK,D>>
extends BaseCounterAdapter<PK,D,N>
implements MapStorageNode<PK,D>, MapStorageCounterAdapterMixin<PK,D,N>{

	public MapStorageCounterAdapter(N backingNode){
		super(backingNode);
	}

}
