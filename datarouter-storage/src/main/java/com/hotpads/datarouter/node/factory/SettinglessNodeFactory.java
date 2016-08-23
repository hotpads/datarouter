package com.hotpads.datarouter.node.factory;

import java.util.function.Supplier;

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
import com.hotpads.util.core.java.ReflectionTool;

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
		return create(clientId, ReflectionTool.supplier(databeanClass), ReflectionTool.supplier(fielderClass), router,
				addAdapter);
	}


	/**
	 * @deprecated use {@link #create(ClientId, Supplier, Supplier, Router, boolean)}
	 */
	@Deprecated
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>>
	N create(
			ClientId clientId,
			Supplier<D> databeanClass,
			Supplier<F> fielderClass,
			Router router,
			boolean addAdapter){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<>(router, databeanClass, fielderClass)
				.withClientId(clientId);
		return create(paramsBuilder.build(), addAdapter);
	}

}
