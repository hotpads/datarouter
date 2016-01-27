package com.hotpads.datarouter.node.factory;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.adapter.callsite.MapStorageCallsiteAdapter;
import com.hotpads.datarouter.node.adapter.counter.MapStorageCounterAdapter;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.caching.map.MapCachingMapStorageNode;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class CachingNodeFactory{

	private final DatarouterClients clients;
	private final DatarouterSettings drSettings;


	@Inject
	private CachingNodeFactory(DatarouterClients clients, DatarouterSettings drSettings){
		this.clients = clients;
		this.drSettings = drSettings;
	}


	/********************* pass any params *****************/

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends MapStorageNode<PK,D>>
	MapStorageNode<PK,D> create(NodeParams<PK,D,F> params,
			N cacheNode,
			N backingNode,
			boolean cacheReads,
			boolean cacheWrites,
			boolean addAdapter){
		String clientName = params.getClientName();
		ClientType clientType = clients.getClientTypeInstance(clientName);
		Preconditions.checkNotNull(clientType, "clientType not found for clientName:"+clientName);
		MapStorageNode<PK,D> node = new MapCachingMapStorageNode<PK,D,F,N>(cacheNode, backingNode, cacheReads,
				cacheWrites);
		node = new MapStorageCounterAdapter<>(node);
		if(addAdapter){
			node = new MapStorageCallsiteAdapter<>(params, (N)node);
		}
		return Preconditions.checkNotNull(node, "cannot build Node for clientType="+clientType);
	}


	/*************** simple helpers *********************/

	// +fielderClass
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends MapStorageNode<PK,D>>
	MapStorageNode<PK,D> create(
			Class<D> databeanClass,
			Router router,
			Class<F> fielderClass,
			N cacheNode,
			N backingNode,
			boolean cacheReads,
			boolean cacheWrites,
			boolean addAdapter){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withFielder(fielderClass)
				.withDiagnostics(drSettings.getRecordCallsites());
		return create(paramsBuilder.build(), cacheNode, backingNode, cacheReads, cacheWrites, addAdapter);
	}
}
