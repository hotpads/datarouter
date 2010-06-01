package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.IndexedSortedStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface PhysicalIndexedSortedStorageNode<D extends Databean<PK>,PK extends PrimaryKey<PK>>
extends PhysicalIndexedSortedStorageReaderNode<D,PK>, PhysicalSortedStorageNode<D,PK>, IndexedSortedStorageNode<D,PK>
{

	
}
