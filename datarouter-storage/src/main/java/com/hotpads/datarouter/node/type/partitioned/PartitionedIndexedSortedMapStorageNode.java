package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.function.Supplier;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedIndexedStorageReaderMixin;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedIndexedStorageWriterMixin;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedMapStorageWriterMixin;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedSortedStorageWriterMixin;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class PartitionedIndexedSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalIndexedSortedMapStorageNode<PK,D>>
extends PartitionedSortedMapStorageReaderNode<PK,D,F,N>
implements IndexedSortedMapStorageNode<PK,D>,
		PartitionedMapStorageWriterMixin<PK,D,N>,
		PartitionedIndexedStorageReaderMixin<PK,D,N>{

	protected PartitionedSortedStorageWriterMixin<PK,D,F,N> mixinSortedWriteOps;
	protected PartitionedIndexedStorageWriterMixin<PK,D,F,N> mixinIndexedWriteOps;

	public PartitionedIndexedSortedMapStorageNode(Supplier<D> databeanSupplier, Supplier<F> fielderSupplier,
			Router router) {
		super(databeanSupplier, fielderSupplier, router);
		this.mixinSortedWriteOps = new PartitionedSortedStorageWriterMixin<>(this);
		this.mixinIndexedWriteOps = new PartitionedIndexedStorageWriterMixin<>(this);
	}

	/**
	 * @deprecated use {@link #PartitionedIndexedSortedMapStorageNode(Supplier, Class, Router)}
	 */
	@Deprecated
	public PartitionedIndexedSortedMapStorageNode(Class<D> databeanClass, Class<F> fielderClass, Router router) {
		this(ReflectionTool.supplier(databeanClass), ReflectionTool.supplier(fielderClass), router);
	}

	@Override
	public void delete(Lookup<PK> lookup, Config config){
		mixinIndexedWriteOps.delete(lookup, config);
	}

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		mixinSortedWriteOps.deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		mixinIndexedWriteOps.deleteMultiUnique(uniqueKeys, config);
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		mixinIndexedWriteOps.deleteUnique(uniqueKey, config);
	}

	@Override
	public <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		mixinIndexedWriteOps.deleteByIndex(keys, config);
	}
}
