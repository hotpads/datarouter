package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.SortedStorageNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface PhysicalSortedStorageNode<D extends Databean<PK>,PK extends PrimaryKey<PK>>
extends PhysicalSortedStorageReaderNode<D,PK>, SortedStorageNode<D,PK>
{

	
}
