package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.IndexedStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;

public interface PhysicalIndexedStorageNode<D extends Databean> 
extends PhysicalIndexedStorageReaderNode<D>, IndexedStorageNode<D>
{

	
}
