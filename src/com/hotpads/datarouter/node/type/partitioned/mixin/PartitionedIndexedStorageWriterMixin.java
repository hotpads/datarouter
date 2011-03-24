package com.hotpads.datarouter.node.type.partitioned.mixin;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.IndexedStorageWriter.PhysicalIndexedStorageWriterNode;
import com.hotpads.datarouter.node.type.partitioned.base.BasePartitionedNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.CollectionTool;

public class PartitionedIndexedStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalIndexedStorageWriterNode<PK,D>>
implements IndexedStorageWriter<PK,D>{
	
	protected BasePartitionedNode<PK,D,N> target;
	
	public PartitionedIndexedStorageWriterMixin(BasePartitionedNode<PK,D,N> target){
		this.target = target;
	}

	@Override
	public void delete(Lookup<PK> lookup, Config config){
		for(N node : CollectionTool.nullSafe(target.getPhysicalNodes())){
			node.delete(lookup, config);
		}
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		for(N node : CollectionTool.nullSafe(target.getPhysicalNodes())){
			node.deleteMultiUnique(uniqueKeys, config);
		}
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		for(N node : CollectionTool.nullSafe(target.getPhysicalNodes())){
			node.deleteUnique(uniqueKey, config);
		}
	}
	
	
}
