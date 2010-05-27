package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.op.IndexedStorageReadOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public interface IndexedStorageReaderNode<D extends Databean,PK extends PrimaryKey<D>>
extends MapStorageReaderNode<D,PK>, IndexedStorageReadOps<D,PK>
{

}
