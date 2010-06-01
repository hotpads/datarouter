package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexedSortedStorageReaderNode<D extends Databean<PK>,PK extends PrimaryKey<PK>>
extends SortedStorageReaderNode<D,PK>, IndexedStorageReaderNode<D,PK>
{

}
