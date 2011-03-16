package com.hotpads.datarouter.node.type.partitioned.mixin;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.partitioned.BasePartitionedNode;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.SortedStorageWriter.PhysicalSortedStorageWriterNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;

public class PartitionedSortedStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalSortedStorageWriterNode<PK,D>>
implements SortedStorageWriter<PK,D>{
	
	protected BasePartitionedNode<PK,D,N> target;
	
	public PartitionedSortedStorageWriterMixin(BasePartitionedNode<PK,D,N> target){
		this.target = target;
	}
	
	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		for(N node : CollectionTool.nullSafe(target.getPhysicalNodes())){
			node.deleteRangeWithPrefix(prefix, wildcardLastField, config);
		}
	}
}
