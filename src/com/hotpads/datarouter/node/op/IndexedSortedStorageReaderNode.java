package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public interface IndexedSortedStorageReaderNode<D extends Databean,PK extends PrimaryKey<D>>
extends SortedStorageReaderNode<D,PK>, IndexedStorageReaderNode<D,PK>
{

}
