package com.hotpads.datarouter.node.adapter.availability;

import com.hotpads.datarouter.node.adapter.PhysicalAdapterMixin;
import com.hotpads.datarouter.node.adapter.availability.mixin.PhysicalMapStorageAvailabilityAdapterMixin;
import com.hotpads.datarouter.node.adapter.availability.mixin.PhysicalSortedStorageAvailabilityAdapterMixin;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class PhysicalSortedMapStorageAvailabilityAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalSortedMapStorageNode<PK,D>>
extends BaseAvailabilityAdapter<PK,D,N>
implements PhysicalSortedMapStorageNode<PK,D>,
		PhysicalMapStorageAvailabilityAdapterMixin<PK,D,N>,
		PhysicalSortedStorageAvailabilityAdapterMixin<PK,D,N>,
		PhysicalAdapterMixin<PK,D,N>{

	public PhysicalSortedMapStorageAvailabilityAdapter(N backingNode){
		super(backingNode);
	}

}
