package com.hotpads.datarouter.node.type.index;

import java.util.ArrayList;
import java.util.List;

import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class ManagedNodesHolder<PK extends PrimaryKey<PK>, D extends Databean<PK,D>>{

	private List<ManagedNode<PK,D,?,?,?>> managedNodes;
	
	public ManagedNodesHolder(){
		this.managedNodes = new ArrayList<>();
	}
	
	public List<ManagedNode<PK,D,?,?,?>> getManagedNodes(){
		return managedNodes;
	}
	
	public <N extends ManagedNode<PK,D,?,?,?>> N registerManagedNode(N node){
		managedNodes.add(node);
		return node;
	}

}
