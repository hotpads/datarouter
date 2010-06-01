package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.op.MapStorageWriteOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface MapStorageNode<D extends Databean<PK>,PK extends PrimaryKey<PK>>
extends MapStorageReaderNode<D,PK>, MapStorageWriteOps<D,PK>
{

}
