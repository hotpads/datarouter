package com.hotpads.datarouter.node.adapter.counter.physical;

import com.hotpads.datarouter.node.adapter.PhysicalAdapterMixin;
import com.hotpads.datarouter.node.adapter.counter.BaseCounterAdapter;
import com.hotpads.datarouter.node.adapter.counter.mixin.MapStorageReaderCounterAdapterMixin;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.PhysicalMapStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class PhysicalMapStorageReaderCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalMapStorageReaderNode<PK,D>>
extends BaseCounterAdapter<PK,D,N>
implements PhysicalMapStorageReaderNode<PK,D>,
		MapStorageReaderCounterAdapterMixin<PK,D,N>,
		PhysicalAdapterMixin<PK,D,N>{

	public PhysicalMapStorageReaderCounterAdapter(N backingNode){
		super(backingNode);
	}

}
