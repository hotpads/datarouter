package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.op.SortedStorageWriteOps;
import com.hotpads.datarouter.storage.databean.Databean;

public interface SortedStorageNode<D extends Databean>
extends SortedStorageReaderNode<D>, MapStorageNode<D>, SortedStorageWriteOps<D>
{

}
