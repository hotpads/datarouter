package com.hotpads.datarouter.client;

import java.util.List;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.entity.EntityNode;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface ClientType{

	@Deprecated//should not reserve names
	String getName();
	
	ClientFactory createClientFactory(Datarouter datarouter, String clientName, 
			List<PhysicalNode<?,?>> physicalNodes);
	
	<PK extends PrimaryKey<PK>, D extends Databean<PK, D>, F extends DatabeanFielder<PK, D>>
	PhysicalNode<PK,D> createNode(NodeParams<PK, D, F> nodeParams);
	
	<EK extends EntityKey<EK>,E extends Entity<EK>>
	EntityNode<EK,E> createEntityNode(NodeFactory nodeFactory, Router router, 
			EntityNodeParams<EK,E> entityNodeParams, String clientName);

	<EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK, D>,
			F extends DatabeanFielder<PK, D>>
	Node<PK,D> createSubEntityNode(EntityNodeParams<EK,E> entityNodeParams, NodeParams<PK,D,F> nodeParams);
	
	<PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>> 
	Node<PK,D> 
	createAdapter(NodeParams<PK,D,F> nodeParams, Node<PK,D> backingNode);
}
