package com.hotpads.datarouter.client.imp.hbase.node;

import com.hotpads.datarouter.client.type.HBaseClient;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface HBasePhysicalNode<PK extends PrimaryKey<PK>,D extends Databean<PK>> 
extends PhysicalNode<PK,D>
{

	@Override
	HBaseClient getClient();
	
}
