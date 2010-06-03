package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.op.IndexedStorageReadOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexedStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends MapStorageReaderNode<PK,D>, IndexedStorageReadOps<PK,D>
{

}
