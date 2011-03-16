package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.PhysicalSortedMapStorageNode;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedMapStorageWriterMixin;
import com.hotpads.datarouter.node.type.partitioned.mixin.PartitionedSortedStorageWriterMixin;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class PartitionedSortedMapStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalSortedMapStorageNode<PK,D>>
extends PartitionedSortedMapStorageReaderNode<PK,D,N>
implements SortedMapStorageNode<PK,D>{

	protected PartitionedMapStorageWriterMixin<PK,D,N> mixinMapWriteOps;
	protected PartitionedSortedStorageWriterMixin<PK,D,N> mixinSortedWriteOps;
	
	public PartitionedSortedMapStorageNode(Class<D> databeanClass, DataRouter router) {
		super(databeanClass, router);
		this.mixinMapWriteOps = new PartitionedMapStorageWriterMixin<PK,D,N>(this);
		this.mixinSortedWriteOps = new PartitionedSortedStorageWriterMixin<PK,D,N>(this);
	}
	
	
	/************************** map ***************************************/

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
	
	/**************************** sorted **********************************/

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		mixinSortedWriteOps.deleteRangeWithPrefix(prefix, wildcardLastField, config);
	}
	
	
	
	

}
