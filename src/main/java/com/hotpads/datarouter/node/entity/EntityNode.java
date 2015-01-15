package com.hotpads.datarouter.node.entity;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;

public interface EntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>{

	DatarouterContext getContext();
	String getName();
	
	Collection<Node<?,?>> getSubEntityNodes();
	
	E getEntity(EK key, Config pConfig);
	
}
