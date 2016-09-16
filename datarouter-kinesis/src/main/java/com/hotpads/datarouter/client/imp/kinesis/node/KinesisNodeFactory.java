package com.hotpads.datarouter.client.imp.kinesis.node;

import javax.inject.Inject;

import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class KinesisNodeFactory{
	@Inject
	private Datarouter datarouter;

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	KinesisNode<PK,D,F> createSingleNode(NodeParams<PK,D,F> params){
		return new KinesisNode<>(datarouter, params);
	}

}
