package com.hotpads.datarouter.node.type.redundant;

import java.util.List;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface RedundantNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends Node<PK,D>>{

	List<N> getWriteNodes();
	N getReadNode();

}
