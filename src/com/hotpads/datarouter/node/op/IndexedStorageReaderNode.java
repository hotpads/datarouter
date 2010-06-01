package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.op.IndexedStorageReadOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface IndexedStorageReaderNode<D extends Databean<PK>,PK extends PrimaryKey<PK>>
extends MapStorageReaderNode<D,PK>, IndexedStorageReadOps<D,PK>
{

}
