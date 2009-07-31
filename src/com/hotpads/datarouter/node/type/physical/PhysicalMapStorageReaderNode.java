package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.MapStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;

public interface PhysicalMapStorageReaderNode<D extends Databean> 
extends PhysicalNode<D>, MapStorageReaderNode<D>
{

	
}
