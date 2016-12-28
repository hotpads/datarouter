package com.hotpads.datarouter.node.type.partitioned;

import java.util.function.Supplier;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.type.partitioned.base.BasePartitionedNode;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedIndexedStorageMixin;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedMapStorageMixin;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedSortedStorageMixin;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class PartitionedIndexedSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalIndexedSortedMapStorageNode<PK,D>>
extends BasePartitionedNode<PK,D,F,N>
implements IndexedSortedMapStorageNode<PK,D>,
		PartitionedMapStorageMixin<PK,D,N>,
		PartitionedIndexedStorageMixin<PK,D,N>,
		PartitionedSortedStorageMixin<PK,D,N>{

	public PartitionedIndexedSortedMapStorageNode(Supplier<D> databeanSupplier, Supplier<F> fielderSupplier,
			Router router){
		super(databeanSupplier, fielderSupplier, router);
	}

	/**
	 * @deprecated use {@link #PartitionedIndexedSortedMapStorageNode(Supplier, Class, Router)}
	 */
	@Deprecated
	public PartitionedIndexedSortedMapStorageNode(Class<D> databeanClass, Class<F> fielderClass, Router router){
		this(ReflectionTool.supplier(databeanClass), ReflectionTool.supplier(fielderClass), router);
	}

}
