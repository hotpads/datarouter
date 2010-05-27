package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.op.MapStorageReadOps;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;

public interface MapStorageReaderNode<D extends Databean,PK extends PrimaryKey<D>>
extends Node<D,PK>, MapStorageReadOps<D,PK>
{

}
