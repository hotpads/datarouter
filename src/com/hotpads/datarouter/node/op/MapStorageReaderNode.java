package com.hotpads.datarouter.node.op;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.op.MapStorageReadOps;
import com.hotpads.datarouter.storage.databean.Databean;

public interface MapStorageReaderNode<D extends Databean>
extends Node<D>, MapStorageReadOps<D>
{

}
