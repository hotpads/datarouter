package com.hotpads.datarouter.client.imp.sqs.group;

import javax.inject.Inject;

import com.hotpads.datarouter.client.imp.sqs.BaseSqsClientType;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.counter.PhysicalGroupQueueStorageCounterAdapater;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class SqsGroupClientType extends BaseSqsClientType{
	
	private static final String NAME = "sqsGroup";
	
	@Inject
	private SqsGroupNodeFactory nodeFactory;

	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public <PK extends PrimaryKey<PK>, 
			D extends Databean<PK, D>,
			F extends DatabeanFielder<PK, D>>
	PhysicalNode<PK, D> createNode(NodeParams<PK, D, F> nodeParams){
		SqsGroupNode<PK,D,F> node = nodeFactory.createNode(nodeParams);
		return new PhysicalGroupQueueStorageCounterAdapater<>(node);
	}

}
