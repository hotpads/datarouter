package com.hotpads.datarouter.node.type.indexing.base;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.op.raw.index.IndexListener;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrSetTool;

public abstract class BaseIndexingNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D>> 
extends BaseNode<PK,D,F>{
	
	protected N mainNode;
	protected List<IndexListener<PK,D>> indexListeners;
	
	public BaseIndexingNode(N mainNode) {
		super(new NodeParamsBuilder<PK,D,F>(mainNode.getRouter(), mainNode.getDatabeanType())
				.withFielder((Class<F>)mainNode.getFieldInfo().getFielderClass())
				.build());
		this.mainNode = mainNode;
		this.indexListeners = DrListTool.createArrayList();
	}
	
	
	public void registerIndexListener(IndexListener<PK,D> indexListener){
		this.indexListeners.add(indexListener);
	}
	
	/*************************** node methods *************************/

	//TODO allow indexes to be on different clients than the master node
	
	@Override
	public Set<String> getAllNames(){
		Set<String> names = DrSetTool.createHashSet();
		names.addAll(DrCollectionTool.nullSafe(getName()));
		names.addAll(DrCollectionTool.nullSafe(mainNode.getAllNames()));
		return names;
	}
	
	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodes(){
		List<PhysicalNode<PK,D>> all = DrListTool.createLinkedList();
		all.addAll(DrListTool.nullSafe(mainNode.getPhysicalNodes()));
		return all;
	}

	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName) {
		List<PhysicalNode<PK,D>> all = DrListTool.createLinkedList();
		all.addAll(DrListTool.nullSafe(mainNode.getPhysicalNodesForClient(clientName)));
		return all;
	}
	

	@Override
	public List<String> getClientNames() {
		SortedSet<String> clientNames = DrSetTool.createTreeSet();
		DrSetTool.nullSafeSortedAddAll(clientNames, mainNode.getClientNames());
		return DrListTool.createArrayList(clientNames);
	}

	@Override
	public boolean usesClient(String clientName){
		return mainNode.usesClient(clientName);
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		Set<String> clientNames = DrSetTool.createHashSet();
		clientNames.addAll(DrCollectionTool.nullSafe(mainNode.getClientNamesForPrimaryKeysForSchemaUpdate(keys)));
		return DrListTool.createArrayList(clientNames);
	}
	
	@Override
	public List<N> getChildNodes(){
		return DrListTool.wrap(mainNode);
	}
	
	@Override
	public Node<PK,D> getMaster() {
		return this;
	}



	public N getBackingNode(){
		return mainNode;
	}
	
	
}
