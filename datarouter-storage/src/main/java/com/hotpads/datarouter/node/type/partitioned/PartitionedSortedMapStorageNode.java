package com.hotpads.datarouter.node.type.partitioned;

import java.util.function.Supplier;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedMapStorageWriterMixin;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedSortedStorageWriterMixin;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class PartitionedSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalSortedMapStorageNode<PK,D>>
extends PartitionedSortedMapStorageReaderNode<PK,D,F,N>
implements SortedMapStorageNode<PK,D>, PartitionedMapStorageWriterMixin<PK,D,N>{

	protected PartitionedSortedStorageWriterMixin<PK,D,F,N> mixinSortedWriteOps;

	public PartitionedSortedMapStorageNode(Supplier<D> databeanSupplier, Supplier<F> fielderSupplier, Router router) {
		super(databeanSupplier, fielderSupplier, router);
		this.mixinSortedWriteOps = new PartitionedSortedStorageWriterMixin<>(this);
	}

	/**
	 * @deprecated use {@link #PartitionedSortedMapStorageNode(Supplier, Class, Router)}
	 */
	@Deprecated
	public PartitionedSortedMapStorageNode(Class<D> databeanClass, Class<F> fielderClass, Router router) {
		this(ReflectionTool.supplier(databeanClass), ReflectionTool.supplier(fielderClass), router);
	}

	/**************************** sorted **********************************/

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		mixinSortedWriteOps.deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}

}
