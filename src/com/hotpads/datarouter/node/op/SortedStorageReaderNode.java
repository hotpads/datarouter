package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.op.SortedStorageReadOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public interface SortedStorageReaderNode<D extends Databean,PK extends PrimaryKey<D>>
extends MapStorageReaderNode<D,PK>, SortedStorageReadOps<D,PK>
{

}
