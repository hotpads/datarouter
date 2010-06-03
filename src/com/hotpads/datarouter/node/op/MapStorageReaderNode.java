package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.op.MapStorageReadOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface MapStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK>>
extends Node<PK,D>, MapStorageReadOps<PK,D>
{

}
