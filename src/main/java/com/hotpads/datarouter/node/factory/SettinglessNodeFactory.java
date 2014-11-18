package com.hotpads.datarouter.node.factory;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class SettinglessNodeFactory{
	private static final Logger logger = LoggerFactory.getLogger(SettinglessNodeFactory.class);

	
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>> 
	N create(NodeParams<PK,D,F> params, boolean addAdapter){
		String clientName = params.getClientName();
		ClientType clientType = params.getRouter().getClientOptions().getClientTypeInstance(clientName);
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
			String clientName, 
			Class<D> databeanClass, 
			Class<F> fielderClass,
			DataRouter router,
			boolean addAdapter){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withClientName(clientName)
				.withFielder(fielderClass);
		return create(paramsBuilder.build(), addAdapter);
	}
	
}
