package com.hotpads.datarouter.client;

import java.util.List;

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

public interface ClientType{

	@Deprecated//should not reserve names
	String getName();
	
	ClientFactory createClientFactory(DataRouterContext drContext, String clientName, 
			List<PhysicalNode<?,?>> physicalNodes);
	
	Node<?,?> createNode(NodeParams<?,?,?> nodeParams);
	Node<?,?> createSubEntityNode(EntityNodeParams<?,?> entityNodeParams, NodeParams<?,?,?> nodeParams);

	<PK extends PrimaryKey<PK>, 
	D extends Databean<PK, D>, 
	IK extends PrimaryKey<IK>, 
	IE extends UniqueIndexEntry<IK, IE, PK, D>,
	IF extends DatabeanFielder<IK, IE>> ManagedUniqueIndexNode<PK, D, IK, IE> createManagedUniqueIndexNode(
				PhysicalMapStorageNode<PK, D> backingMapNode, Class<IE> indexEntryClass, Class<IF> indexFielder, boolean manageTxn);
	
	<PK extends PrimaryKey<PK>, 
	D extends Databean<PK, D>, 
	IK extends PrimaryKey<IK>, 
	IE extends MultiIndexEntry<IK, IE, PK, D>,
	IF extends DatabeanFielder<IK, IE>> ManagedMultiIndexNode<PK, D, IK, IE> createManagedMultiIndexNode(
			PhysicalMapStorageNode<PK, D> backingMapNode, Class<IE> indexEntryClass, Class<IF> indexFielder, boolean manageTxn);
	
}
