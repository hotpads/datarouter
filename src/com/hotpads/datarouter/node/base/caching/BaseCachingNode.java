package com.hotpads.datarouter.node.base.caching;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.base.BaseNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.SetTool;

public abstract class BaseCachingNode<PK extends PrimaryKey<PK>,D extends Databean<PK>,N extends Node<PK,D>> 
extends BaseNode<PK,D>{
	
	
	/***************************** Node pass-through stuff **********************************/
	
	protected N backingNode;
	
	public BaseCachingNode(N backingNode){
		super(backingNode.getDatabeanType());
		this.backingNode = backingNode;
	}
	
	@Override
	public List<String> getClientNames() {
		return this.backingNode.getClientNames();
	}

	@Override
	public List<String> getClientNamesForPrimaryKeys(Collection<PK> keys) {
		return this.backingNode.getClientNamesForPrimaryKeys(keys);
	}

	@Override
	public Class<D> getDatabeanType() {
		return this.backingNode.getDatabeanType();
	}

	@Override
	public Node<PK,D> getMaster() {
		return this.backingNode.getMaster();
	}

	@Override
	public String getName() {
		return this.backingNode.getName();
	}

	@Override
	public Set<String> getAllNames(){
		Set<String> names = SetTool.wrap(this.name);
		names.addAll(this.backingNode.getAllNames());
		return names;
	}

	@Override
	public List<? extends PhysicalNode<PK,D>> getPhysicalNodes() {
		return this.backingNode.getPhysicalNodes();
	}

	@Override
	public List<? extends PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName) {
		return this.backingNode.getPhysicalNodesForClient(clientName);
	}

	@Override
	public boolean usesClient(String clientName) {
		return this.backingNode.usesClient(clientName);
	}

	
	
}
