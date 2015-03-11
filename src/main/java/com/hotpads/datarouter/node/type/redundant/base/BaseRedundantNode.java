package com.hotpads.datarouter.node.type.redundant.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrSetTool;

public abstract class BaseRedundantNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends Node<PK,D>> 
extends BaseNode<PK,D,DatabeanFielder<PK,D>>{
	
	protected List<N> writeNodes = new ArrayList<N>();
	protected N readNode;//needs to be one of the write nodes
	
	public BaseRedundantNode(Class<D> databeanClass, Datarouter router, Collection<N> writeNodes, N readNode){
		super(new NodeParamsBuilder<PK,D,DatabeanFielder<PK,D>>(router, databeanClass).build());
		
		if(DrCollectionTool.isEmpty(writeNodes)){ throw new IllegalArgumentException("writeNodes cannont be empty."); }
		if(readNode==null){ throw new IllegalArgumentException("readNode cannont be null."); }
		if(!writeNodes.contains(readNode)){ throw new IllegalArgumentException("readNode must be in writeNodes."); }
		
		this.writeNodes = DrListTool.createArrayList(writeNodes);
		this.readNode = readNode;
	}

	/*************************** node methods *************************/

	@Override
	public Set<String> getAllNames(){
		Set<String> names = new HashSet<>();
		names.add(getName());
		names.addAll(DrCollectionTool.nullSafe(readNode.getAllNames()));
		for(N backingNode : DrIterableTool.nullSafe(writeNodes)){
			names.addAll(DrCollectionTool.nullSafe(backingNode.getAllNames()));
		}
		return names;
	}
	
	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodes(){
		List<PhysicalNode<PK,D>> all = DrListTool.createLinkedList();
		for(N backingNode : DrCollectionTool.nullSafe(writeNodes)){
			all.addAll(DrListTool.nullSafe(backingNode.getPhysicalNodes()));
		}
		return all;
	}

	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName) {
		List<PhysicalNode<PK,D>> all = DrListTool.createLinkedList();
		for(N backingNode : DrCollectionTool.nullSafe(writeNodes)){
			all.addAll(DrListTool.nullSafe(backingNode.getPhysicalNodesForClient(clientName)));
		}
		return all;
	}
	

	@Override
	public List<String> getClientNames() {
		SortedSet<String> clientNames = new TreeSet<>();
		for(N backingNode : writeNodes){
			DrSetTool.nullSafeSortedAddAll(clientNames, backingNode.getClientNames());
		}
		return DrListTool.createArrayList(clientNames);
	}

	@Override
	public boolean usesClient(String clientName){
		for(N backingNode : DrCollectionTool.nullSafe(writeNodes)){
			if(backingNode.usesClient(clientName)){ return true; }
		}
		return false;
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		Set<String> clientNames = new HashSet<>();
		for(N backingNode : writeNodes){
			clientNames.addAll(DrCollectionTool.nullSafe(backingNode.getClientNamesForPrimaryKeysForSchemaUpdate(keys)));
		}
		return DrListTool.createArrayList(clientNames);
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
