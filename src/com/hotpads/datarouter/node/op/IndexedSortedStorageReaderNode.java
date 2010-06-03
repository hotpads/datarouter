package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexedSortedStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends SortedStorageReaderNode<PK,D>, IndexedStorageReaderNode<PK,D>
{

}
