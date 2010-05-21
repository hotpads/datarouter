package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public interface IndexedSortedStorageNode<D extends Databean,K extends PrimaryKey<D>>
extends IndexedSortedStorageReaderNode<D,K>, SortedStorageNode<D,K>, IndexedStorageNode<D,K>
{

}
