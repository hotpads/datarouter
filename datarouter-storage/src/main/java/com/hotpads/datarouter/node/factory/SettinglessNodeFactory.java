package com.hotpads.datarouter.node.factory;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class SettinglessNodeFactory{
	
	private final DatarouterClients clients;
	
	@Inject
	private SettinglessNodeFactory(DatarouterClients clients){
		this.clients = clients;
	}


	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>> 
	N create(NodeParams<PK,D,F> params, boolean addAdapter){
		String clientName = params.getClientId().getName();
		ClientType clientType = clients.getClientTypeInstance(clientName);
		Preconditions.checkNotNull(clientType, "clientType not found for clientName:"+clientName);
		N node = (N)clientType.createNode(params);
		if(addAdapter){
			node = (N)clientType.createAdapter(params, node);
		}
		return Preconditions.checkNotNull(node, "cannot build Node for clientType="+clientType);
	}
	
	
	// +fielderClass
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>> 
	N create(
			ClientId clientId, 
			Class<D> databeanClass, 
			Class<F> fielderClass,
			Router router,
			boolean addAdapter){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withClientId(clientId)
				.withFielder(fielderClass);
		return create(paramsBuilder.build(), addAdapter);
	}
	
}
