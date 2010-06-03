package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface PhysicalMapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends PhysicalMapStorageReaderNode<PK,D>, MapStorageNode<PK,D>
{

	
}
