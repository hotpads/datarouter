package com.hotpads.datarouter.client.imp.redis.node;

import com.hotpads.datarouter.client.imp.redis.client.RedisClient;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface RedisPhysicalNode<PK extends PrimaryKey<PK>, D extends Databean<PK,D>> extends PhysicalNode<PK,D>{

	@Override
	RedisClient getClient();
}
