package com.hotpads.datarouter.node.entity;

import com.hotpads.datarouter.storage.Entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;

public interface EntityNode<EK extends EntityKey<EK>,E extends Entity<EK>>{

	String getName();
	E getEntity(EK key);
	
}
