package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.IndexedStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface PhysicalIndexedStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends PhysicalIndexedStorageReaderNode<PK,D>, IndexedStorageNode<PK,D>
{

	
}
