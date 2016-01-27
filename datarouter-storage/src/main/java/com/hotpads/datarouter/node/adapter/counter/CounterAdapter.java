package com.hotpads.datarouter.node.adapter.counter;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.adapter.counter.formatter.NodeCounterFormatter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface CounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends Node<PK,D>>{

	NodeCounterFormatter<PK,D,N> getCounter();
	N getBackingNode();

}
