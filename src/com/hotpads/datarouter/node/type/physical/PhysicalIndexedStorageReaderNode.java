package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.IndexedStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface PhysicalIndexedStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends PhysicalMapStorageReaderNode<PK,D>, IndexedStorageReaderNode<PK,D>
{

	
}
