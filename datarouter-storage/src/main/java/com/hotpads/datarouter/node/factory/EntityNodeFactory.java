package com.hotpads.datarouter.node.factory;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.node.entity.EntityNode;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;

@Singleton
public class EntityNodeFactory{
	
	@Inject
	private DatarouterClients clients;
	@Inject
	private NodeFactory nodeFactory;

	public <EK extends EntityKey<EK>,E extends Entity<EK>> 
	EntityNode<EK,E> create(String clientName, Router router, EntityNodeParams<EK,E> params){
		ClientType clientType = clients.getClientTypeInstance(clientName);
		Preconditions.checkNotNull(clientType, "clientType not found for clientName:"+clientName);
		EntityNode<EK,E> entityNode = clientType.createEntityNode(nodeFactory, router, params, clientName);
		return entityNode;
	}
}
