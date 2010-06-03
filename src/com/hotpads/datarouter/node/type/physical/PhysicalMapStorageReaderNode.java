package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.MapStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface PhysicalMapStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends PhysicalNode<PK,D>, MapStorageReaderNode<PK,D>
{

	
}
