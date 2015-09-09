package com.hotpads.datarouter.client.imp.sqs;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.imp.sqs.group.SqsGroupNode;
import com.hotpads.datarouter.client.imp.sqs.single.SqsNode;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class SqsNodeFactory{
	
	@Inject
	private Datarouter datarouterContext;

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	SqsNode<PK,D,F> createSingleNode(NodeParams<PK,D,F> params){
		return new SqsNode<>(datarouterContext, params);
	}
	
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	SqsGroupNode<PK,D,F> createGroupNode(NodeParams<PK,D,F> params){
		return new SqsGroupNode<>(datarouterContext, params);
	}
	
}
