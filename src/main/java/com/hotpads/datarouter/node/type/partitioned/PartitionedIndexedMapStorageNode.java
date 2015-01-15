package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.IndexedMapStorage.IndexedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.IndexedMapStorage.PhysicalIndexedMapStorageNode;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedIndexedStorageWriterMixin;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedMapStorageWriterMixin;
import com.hotpads.datarouter.routing.Datarouter;
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
implements IndexedMapStorageNode<PK,D>{

	protected PartitionedMapStorageWriterMixin<PK,D,F,N> mixinMapWriteOps;
	protected PartitionedIndexedStorageWriterMixin<PK,D,F,N> mixinIndexedWriteOps;
	
	public PartitionedIndexedMapStorageNode(Class<D> databeanClass, Class<F> fielderClass, Datarouter router) {
		super(databeanClass, fielderClass, router);
		this.mixinMapWriteOps = new PartitionedMapStorageWriterMixin<PK,D,F,N>(this);
		this.mixinIndexedWriteOps = new PartitionedIndexedStorageWriterMixin<PK,D,F,N>(this);
	}

	@Override
	public void delete(PK key, Config config){
		mixinMapWriteOps.delete(key, config);
	}

	@Override
	public void deleteAll(Config config){
		mixinMapWriteOps.deleteAll(config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config){
		mixinMapWriteOps.deleteMulti(keys, config);
	}

	@Override
	public void put(D databean, Config config){
		mixinMapWriteOps.put(databean, config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config){
		mixinMapWriteOps.putMulti(databeans, config);
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
