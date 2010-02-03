package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.op.SortedStorageReadOps;
import com.hotpads.datarouter.storage.databean.Databean;

public interface SortedStorageReaderNode<D extends Databean>
extends MapStorageReaderNode<D>, SortedStorageReadOps<D>
{

}
