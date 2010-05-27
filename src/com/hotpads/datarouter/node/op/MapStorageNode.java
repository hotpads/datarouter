package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.op.MapStorageWriteOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public interface MapStorageNode<D extends Databean,PK extends PrimaryKey<D>>
extends MapStorageReaderNode<D,PK>, MapStorageWriteOps<D,PK>
{

}
