package com.hotpads.datarouter.node.adapter.availability;

import com.hotpads.datarouter.node.adapter.PhysicalAdapterMixin;
import com.hotpads.datarouter.node.adapter.availability.mixin.PhysicalIndexedStorageAvailabilityAdapterMixin;
import com.hotpads.datarouter.node.adapter.availability.mixin.PhysicalMapStorageAvailabilityAdapterMixin;
import com.hotpads.datarouter.node.adapter.availability.mixin.PhysicalSortedStorageAvailabilityAdapterMixin;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class PhysicalIndexedSortedMapStorageAvailabilityAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalIndexedSortedMapStorageNode<PK,D>>
extends BaseAvailabilityAdapter<PK,D,N>
implements PhysicalIndexedSortedMapStorageNode<PK,D>,
		PhysicalMapStorageAvailabilityAdapterMixin<PK,D,N>,
		PhysicalSortedStorageAvailabilityAdapterMixin<PK,D,N>,
		PhysicalIndexedStorageAvailabilityAdapterMixin<PK,D,N>,
		PhysicalAdapterMixin<PK,D,N>{

	public PhysicalIndexedSortedMapStorageAvailabilityAdapter(N backingNode){
		super(backingNode);
	}

}
