package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.op.IndexedStorageWriteOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexedStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends IndexedStorageReaderNode<PK,D>, MapStorageNode<PK,D>, IndexedStorageWriteOps<PK,D>
{

}
