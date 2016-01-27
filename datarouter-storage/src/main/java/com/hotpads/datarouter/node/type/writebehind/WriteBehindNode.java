package com.hotpads.datarouter.node.type.writebehind;

import java.util.Queue;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.writebehind.base.WriteWrapper;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface WriteBehindNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,N extends Node<PK,D>>{

	Queue<WriteWrapper<?>> getQueue();
	N getBackingNode();
	boolean handleWriteWrapperInternal(WriteWrapper<?> writeWrapper);

}
