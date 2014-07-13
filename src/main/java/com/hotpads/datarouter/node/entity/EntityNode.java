package com.hotpads.datarouter.node.entity;

import java.util.Map;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;

public interface EntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>{

	DataRouterContext getContext();
	String getName();
	
	Map<String,Node<?,?>> getNodeByTableName();
	
	E getEntity(EK key, Config pConfig);
	
}
