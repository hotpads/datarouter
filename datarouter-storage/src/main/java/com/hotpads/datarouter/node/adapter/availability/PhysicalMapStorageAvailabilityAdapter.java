package com.hotpads.datarouter.node.adapter.availability;

import com.hotpads.datarouter.node.adapter.PhysicalAdapterMixin;
import com.hotpads.datarouter.node.adapter.availability.mixin.PhysicalMapStorageAvailabilityAdapterMixin;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class PhysicalMapStorageAvailabilityAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalMapStorageNode<PK,D>>
extends BaseAvailabilityAdapter<PK,D,N>
implements PhysicalMapStorageNode<PK,D>,
		PhysicalMapStorageAvailabilityAdapterMixin<PK,D,N>,
		PhysicalAdapterMixin<PK,D,N>{

	public PhysicalMapStorageAvailabilityAdapter(N backingNode){
		super(backingNode);
	}

}
