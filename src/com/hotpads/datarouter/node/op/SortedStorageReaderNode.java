package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.op.SortedStorageReadOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface SortedStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends MapStorageReaderNode<PK,D>, SortedStorageReadOps<PK,D>
{

}
