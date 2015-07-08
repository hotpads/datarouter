package com.hotpads.datarouter.client.imp.sqs.group;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class SqsGroupNodeFactory{
	
	@Inject
	private DatarouterContext datarouterContext;

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	SqsGroupNode<PK,D,F> createNode(NodeParams<PK,D,F> params){
		return new SqsGroupNode<>(datarouterContext, params);
	}
}
