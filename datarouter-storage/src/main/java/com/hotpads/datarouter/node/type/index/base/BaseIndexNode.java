package com.hotpads.datarouter.node.type.index.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrSetTool;

public abstract class BaseIndexNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		IK extends PrimaryKey<IK>,
		IE extends IndexEntry<IK,IE,PK,D>,
		N extends Node<IK,IE>>
extends BaseNode<IK,IE,DatabeanFielder<IK,IE>>{


	/***************************** Node pass-through stuff **********************************/

	protected N indexNode;

	public BaseIndexNode(Supplier<IE> indexEntrySupplier, N backingNode){
		super(new NodeParamsBuilder<>(backingNode.getRouter(), indexEntrySupplier)
				.withFielder((Supplier<DatabeanFielder<IK,IE>>)backingNode.getFieldInfo().getFielderSupplier())
				.build());
		this.indexNode = backingNode;
	}

	/***************************** methods ************************************/

	public IE createIndexEntry(){
		return getFieldInfo().getDatabeanSupplier().get();
	}

	/**************************************************************************/

	@Override
	public List<String> getClientNames() {
		return indexNode.getClientNames();
	}

	@Override
	public List<ClientId> getClientIds(){
		return indexNode.getClientIds();
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<IK> keys) {
		return indexNode.getClientNamesForPrimaryKeysForSchemaUpdate(keys);
	}

	@Override
	public Node<IK,IE> getMaster() {
		return indexNode.getMaster();
	}

	@Override
	public List<? extends Node<IK,IE>> getChildNodes(){
		if(indexNode==null){
			return new ArrayList<>();
		}
		return DrListTool.wrap(indexNode);
	}

	@Override
	public String getName() {
		return indexNode==null ? null : indexNode.getName();
	}

	@Override
	public Set<String> getAllNames(){
		Set<String> names = DrSetTool.wrap(getName());
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

	public N getBackingNode(){
		return indexNode;
	}
}
