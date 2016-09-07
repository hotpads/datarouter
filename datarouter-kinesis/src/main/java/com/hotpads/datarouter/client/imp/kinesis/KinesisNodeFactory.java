package com.hotpads.datarouter.client.imp.kinesis;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.client.imp.kinesis.single.KinesisNode;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class KinesisNodeFactory{

	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterClients clients;

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	KinesisNode<PK,D,F> createSingleNode(ClientId clientId, Router router, Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier, String streamName, String regionName){
		NodeParams<PK,D,F> params = new NodeParamsBuilder<>(router, databeanSupplier, fielderSupplier)
			.withClientId(clientId)
			.withStreamName(streamName)
			.withRegionName(regionName)
			.build();
		KinesisClientType clientType = getClientType(params);
		return  (KinesisNode<PK,D,F>)clientType.createNode(params);
	}

	private KinesisClientType getClientType(NodeParams<?,?,?> params){
		String clientName = params.getClientId().getName();
		ClientType clientType = clients.getClientTypeInstance(clientName);
		if(clientType == null){
			throw new NullPointerException("clientType not found for clientName:" + clientName);
		}
		return (KinesisClientType) clientType;
	}

}
