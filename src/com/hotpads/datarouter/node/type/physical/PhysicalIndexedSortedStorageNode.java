package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.IndexedSortedStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;

public interface PhysicalIndexedSortedStorageNode<D extends Databean> 
extends PhysicalIndexedSortedStorageReaderNode<D>, PhysicalSortedStorageNode<D>, IndexedSortedStorageNode<D>
{

	
}
