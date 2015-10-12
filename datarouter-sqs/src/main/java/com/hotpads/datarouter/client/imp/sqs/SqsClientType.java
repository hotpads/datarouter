package com.hotpads.datarouter.client.imp.sqs;

import java.util.List;

import javax.inject.Inject;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.BaseClientType;
import com.hotpads.datarouter.client.imp.QueueClientType;
import com.hotpads.datarouter.client.imp.sqs.group.SqsGroupNode;
import com.hotpads.datarouter.client.imp.sqs.single.SqsNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.counter.physical.PhysicalGroupQueueStorageCounterAdapater;
import com.hotpads.datarouter.node.adapter.counter.physical.PhysicalQueueStorageCounterAdapater;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class SqsClientType extends BaseClientType implements QueueClientType{

	private static final String NAME = "sqs";

	@Inject
	private SqsNodeFactory sqsNodeFactory;
	@Inject
	private ClientAvailabilitySettings clientAvailabilitySettings;

	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK, D>,
			F extends DatabeanFielder<PK, D>>
	PhysicalNode<PK, D> createNode(NodeParams<PK, D, F> nodeParams){
		return createSingleQueueNode(nodeParams);
	}

	@Override
	public ClientFactory createClientFactory(Datarouter datarouter, String clientName,
			List<PhysicalNode<?, ?>> physicalNodes){
		SqsOptions sqsOptions = new SqsOptions(datarouter, clientName);
		return new SqsClientFactory(clientName, this, sqsOptions, clientAvailabilitySettings);
	}

	@Override
	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK, PK>,
			D extends Databean<PK, D>,
			F extends DatabeanFielder<PK, D>>
	Node<PK, D> createSubEntityNode(EntityNodeParams<EK, E> entityNodeParams, NodeParams<PK, D, F> nodeParams){
		return createNode(nodeParams);
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK, D>,
			F extends DatabeanFielder<PK, D>>
	Node<PK, D> createAdapter(NodeParams<PK, D, F> nodeParams, Node<PK, D> backingNode){
		return backingNode;
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D> createSingleQueueNode(NodeParams<PK,D,F> nodeParams){
		SqsNode<PK,D,F> node = sqsNodeFactory.createSingleNode(nodeParams);
		return new PhysicalQueueStorageCounterAdapater<>(node);
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	PhysicalNode<PK,D> createGroupQueueNode(NodeParams<PK,D,F> nodeParams){
		SqsGroupNode<PK,D,F> node = sqsNodeFactory.createGroupNode(nodeParams);
		return new PhysicalGroupQueueStorageCounterAdapater<>(node);
	}

}
