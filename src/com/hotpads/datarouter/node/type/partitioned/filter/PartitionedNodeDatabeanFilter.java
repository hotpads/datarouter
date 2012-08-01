package com.hotpads.datarouter.node.type.partitioned.filter;

import com.hotpads.datarouter.node.type.partitioned.base.BasePartitionedNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.iterable.scanner.filter.Filter;

public class PartitionedNodeDatabeanFilter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalNode<PK,D>>
implements Filter<D>{
	
	protected BasePartitionedNode<PK,D,?,N> partitionedNode;
	protected N partition;
	
	
	public PartitionedNodeDatabeanFilter(BasePartitionedNode<PK,D,?,N> partitionedNode, N partition){
		this.partitionedNode = partitionedNode;
		this.partition = partition;
	}


	@Override
	public boolean include(D d){
		return partition == partitionedNode.getPhysicalNode(d.getKey());
	}
}
