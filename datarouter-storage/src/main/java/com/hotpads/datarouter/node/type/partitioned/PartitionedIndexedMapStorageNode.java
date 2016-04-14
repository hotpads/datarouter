package com.hotpads.datarouter.node.type.partitioned;

import java.util.function.Supplier;

import com.hotpads.datarouter.node.op.combo.IndexedMapStorage.IndexedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.IndexedMapStorage.PhysicalIndexedMapStorageNode;
import com.hotpads.datarouter.node.type.partitioned.base.BasePartitionedNode;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedIndexedStorageMixin;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedMapStorageMixin;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class PartitionedIndexedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalIndexedMapStorageNode<PK,D>>
extends BasePartitionedNode<PK,D,F,N>
implements IndexedMapStorageNode<PK,D>,
		PartitionedMapStorageMixin<PK,D,N>,
		PartitionedIndexedStorageMixin<PK,D,N>{

	public PartitionedIndexedMapStorageNode(Supplier<D> databeanSupplier, Supplier<F> fielderSupplier, Router router) {
		super(databeanSupplier, fielderSupplier, router);
	}

}
