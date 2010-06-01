package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexedSortedStorageNode<D extends Databean<PK>,PK extends PrimaryKey<PK>>
extends IndexedSortedStorageReaderNode<D,PK>, SortedStorageNode<D,PK>, IndexedStorageNode<D,PK>
{

}
