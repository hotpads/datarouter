package com.hotpads.datarouter.node.type.partitioned;

import java.util.function.Supplier;

import com.hotpads.datarouter.node.op.combo.reader.IndexedMapStorageReader.IndexedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.combo.reader.IndexedMapStorageReader.PhysicalIndexedMapStorageReaderNode;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedIndexedStorageReaderMixin;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class PartitionedIndexedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalIndexedMapStorageReaderNode<PK,D>>
extends PartitionedMapStorageReaderNode<PK,D,F,N>
implements IndexedMapStorageReaderNode<PK,D>, PartitionedIndexedStorageReaderMixin<PK,D,N>{

	public PartitionedIndexedMapStorageReaderNode(Supplier<D> databeanSupplier, Supplier<F> fielderSupplier,
			Router router) {
		super(databeanSupplier, fielderSupplier, router);
	}

}
