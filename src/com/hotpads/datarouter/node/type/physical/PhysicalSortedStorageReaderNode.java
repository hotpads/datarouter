package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.SortedStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;

public interface PhysicalSortedStorageReaderNode<D extends Databean> 
extends PhysicalNode<D>, SortedStorageReaderNode<D>
{

	
}
