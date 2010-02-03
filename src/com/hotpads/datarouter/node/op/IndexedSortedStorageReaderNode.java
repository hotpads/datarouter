package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.storage.databean.Databean;

public interface IndexedSortedStorageReaderNode<D extends Databean>
extends SortedStorageReaderNode<D>, IndexedStorageReaderNode<D>
{

}
