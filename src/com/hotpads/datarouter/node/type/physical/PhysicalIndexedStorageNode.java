package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.IndexedStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface PhysicalIndexedStorageNode<D extends Databean<PK>,PK extends PrimaryKey<PK>>
extends PhysicalIndexedStorageReaderNode<D,PK>, IndexedStorageNode<D,PK>
{

	
}
