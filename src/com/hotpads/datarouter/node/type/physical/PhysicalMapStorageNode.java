package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;

public interface PhysicalMapStorageNode<D extends Databean> 
extends PhysicalMapStorageReaderNode<D>, MapStorageNode<D>
{

	
}
