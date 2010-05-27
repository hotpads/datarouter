package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public interface PhysicalMapStorageNode<D extends Databean,PK extends PrimaryKey<D>> 
extends PhysicalMapStorageReaderNode<D,PK>, MapStorageNode<D,PK>
{

	
}
