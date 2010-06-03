package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.IndexedSortedStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface PhysicalIndexedSortedStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends PhysicalIndexedSortedStorageReaderNode<PK,D>, PhysicalSortedStorageNode<PK,D>, IndexedSortedStorageNode<PK,D>
{

	
}
