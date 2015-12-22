package com.hotpads.datarouter.client.imp;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public interface QueueClientType extends ClientType{
	
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D> createSingleQueueNode(NodeParams<PK,D,F> nodeParams);	

	public <PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D> createGroupQueueNode(NodeParams<PK,D,F> nodeParams);
}
