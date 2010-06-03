package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexedSortedStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends IndexedSortedStorageReaderNode<PK,D>, SortedStorageNode<PK,D>, IndexedStorageNode<PK,D>
{

}
