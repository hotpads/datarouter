package com.hotpads.datarouter.node.base.caching;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;

public abstract class BaseCachingNode<D extends Databean<PK>,PK extends PrimaryKey<PK>,
N extends Node<D,PK>> 
implements Node<D,PK> {
	
	
	/***************************** Node pass-through stuff **********************************/
	
	protected N backingNode;
	
	public BaseCachingNode(N backingNode){
		this.backingNode = backingNode;
	}
	
	@Override
	public List<String> getClientNames() {
		return this.backingNode.getClientNames();
	}

	@Override
	public <K extends UniqueKey<PK>> List<String> getClientNamesForKeys(Collection<K> keys) {
		return this.backingNode.getClientNamesForKeys(keys);
	}

	@Override
	public Class<D> getDatabeanType() {
		return this.backingNode.getDatabeanType();
	}

	@Override
	public Node<D,PK> getMaster() {
		return this.backingNode.getMaster();
	}

	@Override
	public String getName() {
		return this.backingNode.getName();
	}

	@Override
	public List<? extends PhysicalNode<D,PK>> getPhysicalNodes() {
		return this.backingNode.getPhysicalNodes();
	}

	@Override
	public List<? extends PhysicalNode<D,PK>> getPhysicalNodesForClient(String clientName) {
		return this.backingNode.getPhysicalNodesForClient(clientName);
	}

	@Override
	public boolean usesClient(String clientName) {
		return this.backingNode.usesClient(clientName);
	}

	
	
}
