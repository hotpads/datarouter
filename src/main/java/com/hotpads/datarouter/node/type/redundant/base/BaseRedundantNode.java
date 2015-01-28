package com.hotpads.datarouter.node.type.redundant.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

public abstract class BaseRedundantNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends Node<PK,D>> 
extends BaseNode<PK,D,DatabeanFielder<PK,D>>{
	
	protected List<N> writeNodes = new ArrayList<N>();
	protected N readNode;//needs to be one of the write nodes
	
	public BaseRedundantNode(Class<D> databeanClass, Datarouter router, Collection<N> writeNodes, N readNode){
		super(new NodeParamsBuilder<PK,D,DatabeanFielder<PK,D>>(router, databeanClass).build());
		
		if(CollectionTool.isEmpty(writeNodes)){ throw new IllegalArgumentException("writeNodes cannont be empty."); }
		if(readNode==null){ throw new IllegalArgumentException("readNode cannont be null."); }
		if(!writeNodes.contains(readNode)){ throw new IllegalArgumentException("readNode must be in writeNodes."); }
		
		this.writeNodes = ListTool.createArrayList(writeNodes);
		this.readNode = readNode;
	}

	/*************************** node methods *************************/

	@Override
	public Set<String> getAllNames(){
		Set<String> names = SetTool.createHashSet();
		names.add(getName());
		names.addAll(CollectionTool.nullSafe(readNode.getAllNames()));
		for(N backingNode : IterableTool.nullSafe(writeNodes)){
			names.addAll(CollectionTool.nullSafe(backingNode.getAllNames()));
		}
		return names;
	}
	
	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodes(){
		List<PhysicalNode<PK,D>> all = ListTool.createLinkedList();
		for(N backingNode : CollectionTool.nullSafe(writeNodes)){
			all.addAll(ListTool.nullSafe(backingNode.getPhysicalNodes()));
		}
		return all;
	}

	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName) {
		List<PhysicalNode<PK,D>> all = ListTool.createLinkedList();
		for(N backingNode : CollectionTool.nullSafe(writeNodes)){
			all.addAll(ListTool.nullSafe(backingNode.getPhysicalNodesForClient(clientName)));
		}
		return all;
	}
	

	@Override
	public List<String> getClientNames() {
		SortedSet<String> clientNames = SetTool.createTreeSet();
		for(N backingNode : writeNodes){
			SetTool.nullSafeSortedAddAll(clientNames, backingNode.getClientNames());
		}
		return ListTool.createArrayList(clientNames);
	}

	@Override
	public boolean usesClient(String clientName){
		for(N backingNode : CollectionTool.nullSafe(writeNodes)){
			if(backingNode.usesClient(clientName)){ return true; }
		}
		return false;
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		Set<String> clientNames = SetTool.createHashSet();
		for(N backingNode : writeNodes){
			clientNames.addAll(CollectionTool.nullSafe(backingNode.getClientNamesForPrimaryKeysForSchemaUpdate(keys)));
		}
		return ListTool.createArrayList(clientNames);
	}
	
	@Override
	public List<N> getChildNodes(){
		return writeNodes;
	}
	
	@Override
	public Node<PK,D> getMaster() {
		return this;
	}

	public List<N> getWriteNodes(){
		return writeNodes;
	}
	
	
}
