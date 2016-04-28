package com.hotpads.datarouter.node.type.partitioned;

import java.util.function.Supplier;

import com.hotpads.datarouter.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.type.partitioned.base.BasePartitionedNode;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedMapStorageMixin;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedSortedStorageMixin;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class PartitionedSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalSortedMapStorageNode<PK,D>>
extends BasePartitionedNode<PK,D,F,N>
implements SortedMapStorageNode<PK,D>,
		PartitionedMapStorageMixin<PK,D,N>,
		PartitionedSortedStorageMixin<PK,D,N>{

	public PartitionedSortedMapStorageNode(Supplier<D> databeanSupplier, Supplier<F> fielderSupplier, Router router,
			String name){
		super(databeanSupplier, fielderSupplier, router, name);
	}

}
