package com.hotpads.datarouter.client.imp;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.node.entity.EntityNode;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;

public abstract class BaseClientType implements ClientType{

	@Override
	public <EK extends EntityKey<EK>,E extends Entity<EK>>EntityNode<EK,E> createEntityNode(NodeFactory nodeFactory, 
			Router router, EntityNodeParams<EK,E> entityNodeParams, String clientName){
		throw new UnsupportedOperationException();
	}

}
