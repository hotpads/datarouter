package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.op.MapStorageWriteOps;
import com.hotpads.datarouter.storage.databean.Databean;

public interface MapStorageNode<D extends Databean>
extends MapStorageReaderNode<D>, MapStorageWriteOps<D>
{

}
