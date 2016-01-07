package com.hotpads.datarouter.node.adapter.counter;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.adapter.BaseAdapter;
import com.hotpads.datarouter.node.adapter.counter.formatter.NodeCounterFormatter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class BaseCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends Node<PK,D>>
extends BaseAdapter<PK,D,N>
implements CounterAdapter<PK,D,N>{

	protected NodeCounterFormatter<PK,D,N> counter;

	public BaseCounterAdapter(N backingNode){
		super(backingNode);
		this.counter = new NodeCounterFormatter<>(backingNode);
	}

	@Override
	protected String getToStringPrefix(){
		return "CounterAdapter";
	}

	@Override
	public NodeCounterFormatter<PK,D,N> getCounter(){
		return counter;
	}

	@Override
	public N getBackingNode(){
		return backingNode;
	}
}
