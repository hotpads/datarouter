package com.hotpads.datarouter.client.imp.http;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.availability.ClientAvailabilitySettings;
import com.hotpads.datarouter.client.imp.BaseClientType;
import com.hotpads.datarouter.client.imp.http.node.HttpReaderNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.callsite.physical.PhysicalMapStorageReaderCallsiteAdapter;
import com.hotpads.datarouter.node.adapter.counter.physical.PhysicalMapStorageReaderCounterAdapter;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.PhysicalMapStorageReaderNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class HttpClientType extends BaseClientType{

	public static final String NAME = "http";

	public static HttpClientType INSTANCE;

	private final ClientAvailabilitySettings clientAvailabilitySettings;

	@Inject
	public HttpClientType(ClientAvailabilitySettings clientAvailabilitySettings){
		this.clientAvailabilitySettings = clientAvailabilitySettings;
		INSTANCE = this;
	}

	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public ClientFactory createClientFactory(Datarouter datarouter, String clientName,
			List<PhysicalNode<?,?>> physicalNodes){
		return new DatarouterHttpClientFactory(datarouter, clientName, clientAvailabilitySettings);
	}

	@Override
	public <PK extends PrimaryKey<PK>, D extends Databean<PK, D>, F extends DatabeanFielder<PK, D>>
	PhysicalNode<PK, D> createNode(NodeParams<PK, D, F> nodeParams){
		return new PhysicalMapStorageReaderCounterAdapter<>(new HttpReaderNode<>(nodeParams));
	}

	//ignore the entityNodeParams
	@Override
	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK, D>,
			F extends DatabeanFielder<PK, D>>
	Node<PK,D> createSubEntityNode(EntityNodeParams<EK,E> entityNodeParams, NodeParams<PK,D,F> nodeParams){
		return createNode(nodeParams);
	}

	@Override
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	MapStorageReaderNode<PK,D> createAdapter(NodeParams<PK,D,F> nodeParams, Node<PK,D> backingNode){
		return new PhysicalMapStorageReaderCallsiteAdapter<>(nodeParams,
				(PhysicalMapStorageReaderNode<PK,D>) backingNode);
	}
}
