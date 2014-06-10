package com.hotpads.datarouter.node.entity;

import com.hotpads.datarouter.storage.key.entity.EntityKey;

public interface EntityNode<EK extends EntityKey<EK>>{

	String getName();
	
}
