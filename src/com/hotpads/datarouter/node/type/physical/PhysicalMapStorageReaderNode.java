package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.MapStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface PhysicalMapStorageReaderNode<D extends Databean<PK>,PK extends PrimaryKey<PK>>
extends PhysicalNode<D,PK>, MapStorageReaderNode<D,PK>
{

	
}
