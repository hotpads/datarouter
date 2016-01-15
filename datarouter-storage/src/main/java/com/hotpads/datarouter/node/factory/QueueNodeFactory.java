package com.hotpads.datarouter.node.factory;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.client.imp.QueueClientType;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class QueueNodeFactory{

	@Inject
	private DatarouterClients clients;

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>>
	N createSingleQueueNode(ClientId clientId, Router router, Class<D> databeanClass, String queueName,
			Class<F> fielder, boolean addAdapter){
		NodeParams<PK,D,F> params = new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withClientId(clientId)
				.withFielder(fielder)
				.withTableName(queueName)
				.build();
		QueueClientType clientType = getClientType(params);
		return wrapWithAdapterIfNecessary(clientType, clientType.createSingleQueueNode(params), addAdapter, params);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>>
	N createGroupQueueNode(ClientId clientId, Router router, Class<D> databeanClass, String queueName,
			Class<F> fielder, boolean addAdapter){
		NodeParams<PK,D,F> params = new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withClientId(clientId)
				.withFielder(fielder)
				.withTableName(queueName)
				.build();
		QueueClientType clientType = getClientType(params);
		return wrapWithAdapterIfNecessary(clientType, clientType.createGroupQueueNode(params), addAdapter, params);
	}

	private QueueClientType getClientType(NodeParams<?,?,?> params){
		String clientName = params.getClientId().getName();
		ClientType clientType = clients.getClientTypeInstance(clientName);
		if(clientType == null){
			throw new NullPointerException("clientType not found for clientName:" + clientName);
		}
		return (QueueClientType) clientType;
	}

	@SuppressWarnings("unchecked")
	private static <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>>
	N wrapWithAdapterIfNecessary(ClientType clientType, PhysicalNode<PK, D> node, boolean addAdapter,
			NodeParams<PK,D,F> params){
		if(addAdapter){
			return (N) clientType.createAdapter(params, node);
		}
		return (N) node;
	}
}
