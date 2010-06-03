package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.op.SortedStorageWriteOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface SortedStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends SortedStorageReaderNode<PK,D>, MapStorageNode<PK,D>, SortedStorageWriteOps<PK,D>
{

}
