package com.hotpads.datarouter.client.imp.memory;

import java.util.List;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.memory.node.HashMapNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedMultiIndexNode;
import com.hotpads.datarouter.node.type.index.ManagedUniqueIndexNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.multi.MultiIndexEntry;
import com.hotpads.datarouter.storage.view.index.unique.UniqueIndexEntry;

@Singleton
public class MemoryClientType
implements ClientType{
	
	public static final String NAME = "memory";
	
	public static final MemoryClientType INSTANCE = new MemoryClientType();
	
	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public ClientFactory createClientFactory(DataRouterContext drContext, String clientName,
			List<PhysicalNode<?,?>> physicalNodes){
		return new MemoryClientFactory(clientName);
	}
	
	@Override
	public Node<?,?> createNode(NodeParams<?,?,?> nodeParams){
		return new HashMapNode(nodeParams);
	}
	
	//ignore the entityNodeParams
	@Override
	public Node<?,?> createSubEntityNode(EntityNodeParams<?,?> entityNodeParams, NodeParams<?,?,?> nodeParams){
		return createNode(nodeParams);
	}
	
	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, IK extends PrimaryKey<IK>, 
	IE extends UniqueIndexEntry<IK, IE, PK, D>, IF extends DatabeanFielder<IK, IE>>
	ManagedUniqueIndexNode<PK, D, IK, IE> createManagedUniqueIndexNode(
			PhysicalMapStorageNode<PK, D> backingMapNode, Class<IE> indexEntryClass, Class<IF> indexFielder, boolean manageTxn){
		throw new UnsupportedOperationException(getName() + " does not support managed indexes");
	}
	
	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, IK extends PrimaryKey<IK>, 
	IE extends MultiIndexEntry<IK, IE, PK, D>, IF extends DatabeanFielder<IK, IE>>
	ManagedMultiIndexNode<PK, D, IK, IE> createManagedMultiIndexNode(
			PhysicalMapStorageNode<PK, D> backingMapNode, Class<IE> indexEntryClass, Class<IF> indexFielder, boolean manageTxn){
		throw new UnsupportedOperationException(getName() + " does not support managed indexes");
	}
	
}
