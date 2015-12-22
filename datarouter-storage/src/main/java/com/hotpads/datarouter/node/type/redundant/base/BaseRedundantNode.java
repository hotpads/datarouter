package com.hotpads.datarouter.node.type.redundant.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.node.type.redundant.RedundantNode;
import com.hotpads.datarouter.routing.Router;
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
extends BaseNode<PK,D,DatabeanFielder<PK,D>>
implements RedundantNode<PK,D,N>{

	protected List<N> writeNodes = new ArrayList<>();
	protected N readNode;//needs to be one of the write nodes

	public BaseRedundantNode(Supplier<D> databeanSupplier, Router router, Collection<N> writeNodes, N readNode){
		super(new NodeParamsBuilder<>(router, databeanSupplier)
				.withFielder((Supplier<DatabeanFielder<PK,D>>)readNode.getFieldInfo().getFielderSupplier())
				.build());

		if(DrCollectionTool.isEmpty(writeNodes)){
			throw new IllegalArgumentException("writeNodes cannont be empty.");
		}
		if(!writeNodes.contains(readNode)){
			throw new IllegalArgumentException("readNode must be in writeNodes.");
		}

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
		List<PhysicalNode<PK,D>> all = new LinkedList<>();
		for(N backingNode : DrCollectionTool.nullSafe(writeNodes)){
			all.addAll(DrListTool.nullSafe(backingNode.getPhysicalNodes()));
		}
		return all;
	}

	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName) {
		List<PhysicalNode<PK,D>> all = new LinkedList<>();
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
	public List<ClientId> getClientIds(){
		Set<ClientId> clientIds = new HashSet<>();
		for(N backingNode : writeNodes){
			clientIds.addAll(backingNode.getClientIds());
		}
		return new ArrayList<>(clientIds);
	}

	@Override
	public boolean usesClient(String clientName){
		for(N backingNode : DrCollectionTool.nullSafe(writeNodes)){
			if(backingNode.usesClient(clientName)){
				return true;
			}
		}
		return false;
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		Set<String> clientNames = new HashSet<>();
		for(N backingNode : writeNodes){
			clientNames.addAll(DrCollectionTool.nullSafe(
					backingNode.getClientNamesForPrimaryKeysForSchemaUpdate(keys)));
		}
		return DrListTool.createArrayList(clientNames);
	}

	@Override
	public List<N> getChildNodes(){
		return writeNodes;
	}

	@Override
	public Node<PK,D> getMaster() {
		return readNode;
	}

	@Override
	public List<N> getWriteNodes(){
		return writeNodes;
	}

	@Override
	public N getReadNode(){
		return readNode;
	}

}
