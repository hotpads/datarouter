package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.SortedStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;

public interface PhysicalSortedStorageNode<D extends Databean> 
extends PhysicalSortedStorageReaderNode<D>, SortedStorageNode<D>
{

	
}
