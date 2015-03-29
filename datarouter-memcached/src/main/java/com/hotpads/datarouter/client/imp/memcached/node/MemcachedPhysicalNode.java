package com.hotpads.datarouter.client.imp.memcached.node;

import com.hotpads.datarouter.client.imp.memcached.client.MemcachedClient;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface MemcachedPhysicalNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>> 
extends PhysicalNode<PK,D>
{

	@Override
	MemcachedClient getClient();
	
}
