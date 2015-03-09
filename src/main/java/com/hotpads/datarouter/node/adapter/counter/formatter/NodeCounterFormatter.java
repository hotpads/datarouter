package com.hotpads.datarouter.node.adapter.counter.formatter;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;

public class NodeCounterFormatter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D>>{

	
	private final N node;
	private final PhysicalNode<PK,D> physicalNode;//may be null
	
	
	/******************* construct *************************/
	
	public NodeCounterFormatter(N node){
		this.node = node;
		if(node instanceof PhysicalNode){
			this.physicalNode = (PhysicalNode)node;
		}else{
			this.physicalNode = null;
		}
	}
	
	
	/********************** methods *****************************/
	
	public void count(String key){
		count(key, 1);
	}
	
	public void count(String key, long delta){
		if(physicalNode == null){
			DRCounters.incNode(key, node.getName(), delta);
		}else{
			DRCounters.incFromCounterAdapter(physicalNode, key, delta);
		}
	}

}
