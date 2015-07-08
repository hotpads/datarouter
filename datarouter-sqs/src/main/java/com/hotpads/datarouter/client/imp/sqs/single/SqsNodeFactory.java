package com.hotpads.datarouter.client.imp.sqs.single;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class SqsNodeFactory{
	
	@Inject
	private DatarouterContext datarouterContext;

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	SqsNode<PK,D,F> createNode(NodeParams<PK,D,F> params){
		return new SqsNode<>(datarouterContext, params);
	}
	
}
