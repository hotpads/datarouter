package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.function.Supplier;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.IndexedMapStorage.IndexedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.IndexedMapStorage.PhysicalIndexedMapStorageNode;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedIndexedStorageWriterMixin;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedMapStorageWriterMixin;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public abstract class PartitionedIndexedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalIndexedMapStorageNode<PK,D>>
extends PartitionedIndexedMapStorageReaderNode<PK,D,F,N>
implements IndexedMapStorageNode<PK,D>, PartitionedMapStorageWriterMixin<PK,D,N>{

	protected PartitionedIndexedStorageWriterMixin<PK,D,F,N> mixinIndexedWriteOps;

	public PartitionedIndexedMapStorageNode(Supplier<D> databeanSupplier, Supplier<F> fielderSupplier, Router router) {
		super(databeanSupplier, fielderSupplier, router);
		this.mixinIndexedWriteOps = new PartitionedIndexedStorageWriterMixin<>(this);
	}

	@Override
	public void delete(Lookup<PK> lookup, Config config){
		mixinIndexedWriteOps.delete(lookup, config);
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		mixinIndexedWriteOps.deleteMultiUnique(uniqueKeys, config);
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		mixinIndexedWriteOps.deleteUnique(uniqueKey, config);
	}

}
