package com.hotpads.datarouter.node.type.physical;

import com.hotpads.datarouter.node.op.SortedStorageReaderNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public interface PhysicalSortedStorageReaderNode<D extends Databean,PK extends PrimaryKey<D>> 
extends PhysicalMapStorageReaderNode<D,PK>, SortedStorageReaderNode<D,PK>
{

	
}
