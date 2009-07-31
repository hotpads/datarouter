package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.storage.databean.Databean;

public interface IndexedStorageNode<D extends Databean>
extends MapStorageNode<D>, IndexedStorageReaderNode<D>//eventually add IndexStorageWriteOps
{

}
