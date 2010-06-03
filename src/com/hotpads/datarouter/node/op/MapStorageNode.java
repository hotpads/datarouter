package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.op.MapStorageWriteOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface MapStorageNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends MapStorageReaderNode<PK,D>, MapStorageWriteOps<PK,D>
{

}
