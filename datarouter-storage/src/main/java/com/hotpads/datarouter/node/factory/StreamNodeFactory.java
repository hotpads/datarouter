package com.hotpads.datarouter.node.factory;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.client.imp.StreamClientType;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class StreamNodeFactory{

	private final DatarouterClients clients;

	@Inject
	public StreamNodeFactory(DatarouterClients clients){
		this.clients = clients;
	}

	@SuppressWarnings("unchecked")
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,N extends Node<PK,D>> N
			createSingleStreamNode(ClientId clientId, Router router, Supplier<D> databeanSupplier,
					Supplier<F> fielderSupplier, String streamName, String regionName){
		NodeParams<PK,D,F> params = new NodeParamsBuilder<>(router, databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withStreamName(streamName)
				.withRegionName(regionName)
				.withTableName(streamName)
				.build();
		StreamClientType clientType = getClientType(params);
		return (N)clientType.createSingleStreamNode(params);
	}

	private StreamClientType getClientType(NodeParams<?,?,?> params){
		String clientName = params.getClientId().getName();
		ClientType clientType = clients.getClientTypeInstance(clientName);
		if(clientType == null){
			throw new NullPointerException("clientType not found for clientName:" + clientName);
		}
		return (StreamClientType)clientType;
	}

}
