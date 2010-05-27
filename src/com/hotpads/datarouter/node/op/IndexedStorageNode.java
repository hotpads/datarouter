package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.op.IndexedStorageWriteOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public interface IndexedStorageNode<D extends Databean,PK extends PrimaryKey<D>>
extends IndexedStorageReaderNode<D,PK>, MapStorageNode<D,PK>, IndexedStorageWriteOps<D,PK>
{

}
