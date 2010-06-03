package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.IndexedStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface PhysicalIndexedSortedStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends PhysicalSortedStorageReaderNode<PK,D>, IndexedStorageReaderNode<PK,D>
{

	
}
