package com.hotpads.datarouter.node.type.index.base;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class BaseIndexNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>,
		N extends Node<IK,IE>>
extends BaseNode<IK,IE,DatabeanFielder<IK,IE>>{
	
	
	/***************************** Node pass-through stuff **********************************/
	
	protected N indexNode;
	
	public BaseIndexNode(Class<IE> indexEntryClass, N backingNode){
		super(backingNode.getRouter(), indexEntryClass, 
				(Class<DatabeanFielder<IK,IE>>)backingNode.getFieldInfo().getFielderClass());
		this.indexNode = backingNode;
	}

	/***************************** methods ************************************/
	
	public IE createIndexEntry(){
		return ReflectionTool.create(getDatabeanType());
	}
	
	
	/**************************************************************************/
	
	@Override
	public List<String> getClientNames() {
		return indexNode.getClientNames();
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<IK> keys) {
		return indexNode.getClientNamesForPrimaryKeysForSchemaUpdate(keys);
	}

	@Override
	public Class<IE> getDatabeanType() {
		return indexNode.getDatabeanType();
	}

	@Override
	public Node<IK,IE> getMaster() {
		return indexNode.getMaster();
	}
	
	@Override
	public List<? extends Node<IK,IE>> getChildNodes(){
		if(indexNode==null){ return ListTool.create(); }
		return ListTool.wrap(indexNode);
	}

	@Override
	public String getName() {
		return indexNode==null ? null : indexNode.getName();
	}

	@Override
	public Set<String> getAllNames(){
		Set<String> names = SetTool.wrap(getName());
		names.addAll(indexNode.getAllNames());
		return names;
	}

	@Override
	public List<? extends PhysicalNode<IK,IE>> getPhysicalNodes() {
		return indexNode.getPhysicalNodes();
	}

	@Override
	public List<? extends PhysicalNode<IK,IE>> getPhysicalNodesForClient(String clientName) {
		return indexNode.getPhysicalNodesForClient(clientName);
	}

	@Override
	public boolean usesClient(String clientName) {
		return indexNode.usesClient(clientName);
	}
	
	@Override
	public void clearThreadSpecificState(){
		indexNode.clearThreadSpecificState();
	}

	public N getBackingNode(){
		return indexNode;
	}

	
	
}