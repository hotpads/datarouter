package com.hotpads.datarouter.client.imp.kinesis;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientId;
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

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	KinesisNode<PK,D,F> createSingleNode(ClientId clientId, Router router, Supplier<D> databeanSupplier,
			String queueName, Supplier<F> fielderSupplier, String arnRole, String streamName, String regionName){
		NodeParams<PK,D,F> params = new NodeParamsBuilder<>(router, databeanSupplier, fielderSupplier)
			.withClientId(clientId)
			.withArnRole(arnRole)
			.withStreamName(streamName)
			.withRegionName(regionName)
			.build();
		return createSingleNode(params);
	}

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	KinesisNode<PK,D,F> createSingleNode(NodeParams<PK,D,F> params){
		return new KinesisNode<>(datarouter, params);
	}

}
