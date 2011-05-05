package com.hotpads.datarouter.node.type.indexing.base;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.raw.index.IndexListener;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

public abstract class BaseIndexingNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends Node<PK,D>> 
extends BaseNode<PK,D,DatabeanFielder<PK,D>>{
	
	protected N mainNode;
	protected List<IndexListener<PK,D>> indexListeners;
	
	public BaseIndexingNode(N mainNode) {
		super(Preconditions.checkNotNull(mainNode).getDatabeanType());
		this.mainNode = mainNode;
		this.indexListeners = ListTool.createArrayList();
	}
	
	
	public void registerIndexListener(IndexListener<PK,D> indexListener){
		this.indexListeners.add(indexListener);
	}
	
	/*************************** node methods *************************/

	//TODO allow indexes to be on different clients than the master node
	
	@Override
	public Set<String> getAllNames(){
		Set<String> names = SetTool.createHashSet();
		names.addAll(CollectionTool.nullSafe(name));
		names.addAll(CollectionTool.nullSafe(mainNode.getAllNames()));
		return names;
	}
	
	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodes(){
		List<PhysicalNode<PK,D>> all = ListTool.createLinkedList();
		all.addAll(ListTool.nullSafe(mainNode.getPhysicalNodes()));
		return all;
	}

	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName) {
		List<PhysicalNode<PK,D>> all = ListTool.createLinkedList();
		all.addAll(ListTool.nullSafe(mainNode.getPhysicalNodesForClient(clientName)));
		return all;
	}
	

	@Override
	public List<String> getClientNames() {
		SortedSet<String> clientNames = SetTool.createTreeSet();
		SetTool.nullSafeSortedAddAll(clientNames, mainNode.getClientNames());
		return ListTool.createArrayList(clientNames);
	}

	@Override
	public boolean usesClient(String clientName){
		return mainNode.usesClient(clientName);
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		Set<String> clientNames = SetTool.createHashSet();
		clientNames.addAll(CollectionTool.nullSafe(mainNode.getClientNamesForPrimaryKeysForSchemaUpdate(keys)));
		return ListTool.createArrayList(clientNames);
	}
	
	@Override
	public void clearThreadSpecificState(){
		mainNode.clearThreadSpecificState();
	}
	
	@Override
	public List<N> getChildNodes(){
		return ListTool.wrap(mainNode);
	}
	
	@Override
	public Node<PK,D> getMaster() {
		return this;
	}



	public N getBackingNode(){
		return mainNode;
	}
	
	
}
