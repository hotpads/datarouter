package com.hotpads.datarouter.node.type.partitioned;

import java.util.function.Supplier;

import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedMapStorageWriterMixin;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class PartitionedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalMapStorageNode<PK,D>>
extends PartitionedMapStorageReaderNode<PK,D,F,N>
implements MapStorageNode<PK,D>, PartitionedMapStorageWriterMixin<PK,D,N>{

	public PartitionedMapStorageNode(Supplier<D> databeanSupplier, Supplier<F> fielderSupplier, Router router) {
		super(databeanSupplier, fielderSupplier, router);
	}

}
