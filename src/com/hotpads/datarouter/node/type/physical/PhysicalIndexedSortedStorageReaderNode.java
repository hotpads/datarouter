package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.IndexedStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;

public interface PhysicalIndexedSortedStorageReaderNode<D extends Databean> 
extends PhysicalSortedStorageReaderNode<D>, IndexedStorageReaderNode<D>
{

	
}
