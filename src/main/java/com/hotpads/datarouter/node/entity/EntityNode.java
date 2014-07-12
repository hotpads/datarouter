package com.hotpads.datarouter.node.entity;

import com.hotpads.datarouter.client.Client;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;

public interface EntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>{

	DataRouterContext getContext();
	String getClientName();
	String getTableName();
	String getName();
	
	Client getClient();
	
	E getEntity(EK key, Config pConfig);
	
}
