package com.hotpads.datarouter.client.imp.kinesis;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.imp.kinesis.single.KinesisNode;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
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
