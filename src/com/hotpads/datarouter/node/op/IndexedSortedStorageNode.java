package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.storage.databean.Databean;

public interface IndexedSortedStorageNode<D extends Databean>
extends IndexedSortedStorageReaderNode<D>, SortedStorageNode<D>, IndexedStorageNode<D>
{

}
