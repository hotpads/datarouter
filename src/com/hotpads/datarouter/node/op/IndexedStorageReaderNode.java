package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.op.IndexedStorageReadOps;
import com.hotpads.datarouter.storage.databean.Databean;

public interface IndexedStorageReaderNode<D extends Databean>
extends MapStorageReaderNode<D>, IndexedStorageReadOps<D>
{

}
