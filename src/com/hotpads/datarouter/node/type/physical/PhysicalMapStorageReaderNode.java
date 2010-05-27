package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.MapStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public interface PhysicalMapStorageReaderNode<D extends Databean,PK extends PrimaryKey<D>> 
extends PhysicalNode<D,PK>, MapStorageReaderNode<D,PK>
{

	
}
