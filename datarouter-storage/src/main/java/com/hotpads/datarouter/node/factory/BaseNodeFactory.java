package com.hotpads.datarouter.node.factory;

import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.routing.BaseRouter;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.setting.Setting;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.java.ReflectionTool;

public abstract class BaseNodeFactory{

	private final DatarouterClients clients;

	public BaseNodeFactory(DatarouterClients clients){
		this.clients = clients;
	}

	/********************* pass any params *****************/

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>>
	N create(NodeParams<PK,D,F> params, boolean addAdapter){
		String clientName = params.getClientId().getName();
		ClientType clientType = clients.getClientTypeInstance(clientName);
		Preconditions.checkNotNull(clientType, "clientType not found for clientName:"+clientName);
		Node<PK, D> node = clientType.createNode(params);
		if(addAdapter){
			node = clientType.createAdapter(params, node);
		}
		return (N) Preconditions.checkNotNull(node, "cannot build Node for clientType="+clientType);
	}


	/*************** simple helpers *********************/

	/**
	 * @deprecated use {@link BaseRouter#create(ClientId, Supplier, Supplier)}
	 */
	@Deprecated
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,N extends Node<PK,D>>
	N create(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier, Router router,
			boolean addAdapter){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<>(router, databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withDiagnostics(getRecordCallsites());
		return create(paramsBuilder.build(), addAdapter);
	}

	/**
	 * @deprecated use {@link BaseRouter#create(ClientId, Supplier, Supplier)}
	 */
	@Deprecated
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,N extends Node<PK,D>>
	N create(
			ClientId clientId,
			Class<D> databeanClass,
			Class<F> fielderClass,
			Router router,
			boolean addAdapter){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<>(router,
				ReflectionTool.supplier(databeanClass), ReflectionTool.supplier(fielderClass))
				.withClientId(clientId)
				.withDiagnostics(getRecordCallsites());
		return create(paramsBuilder.build(), addAdapter);
	}

	/************ include tableName **********************/

	/**
	 * @deprecated use {@link BaseRouter#create(ClientId, Supplier, Supplier)}
	 * and {@link BaseRouter.NodeBuilder#withTableName}
	 */
	@Deprecated
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>>
	N create(//specify tableName and entityName
			ClientId clientId,
			String tableName,
			Class<D> databeanClass,
			Class<F> fielderClass,
			Router router,
			boolean addAdapter){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<>(router,
				ReflectionTool.supplier(databeanClass), ReflectionTool.supplier(fielderClass))
				.withClientId(clientId)
				.withTableName(tableName)
				.withDiagnostics(getRecordCallsites());
		return create(paramsBuilder.build(), addAdapter);
	}


	/***************** entity ***************************/

	/**
	 * @deprecated use {@link #subEntityNode(Router, EntityNodeParams, ClientId, Supplier, Supplier, String)}
	 */
	@Deprecated
	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>>
	N subEntityNode(//specify entityName and entityNodePrefix
			Router router,
			EntityNodeParams<EK,E> entityNodeParams,
			ClientId clientId,
			Class<D> databeanClass,
			Class<F> fielderClass,
			String entityNodePrefix
			){
		return subEntityNode(router, entityNodeParams, clientId, ReflectionTool.supplier(databeanClass),
				ReflectionTool.supplier(fielderClass), entityNodePrefix);
	}

	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>>
	N subEntityNode(//specify entityName and entityNodePrefix
			Router router,
			EntityNodeParams<EK,E> entityNodeParams,
			ClientId clientId,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			String entityNodePrefix
			){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<>(router, databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withParentName(entityNodeParams.getNodeName())
				.withEntity(entityNodeParams.getEntityTableName(), entityNodePrefix)
				.withDiagnostics(getRecordCallsites());
		NodeParams<PK,D,F> nodeParams = paramsBuilder.build();
		ClientType clientType = clients.getClientTypeInstance(clientId.getName());
		Preconditions.checkNotNull(clientType, "clientType not found for clientName:"+clientId.getName());
		Node<PK,D> node = clientType.createSubEntityNode(entityNodeParams, nodeParams);
		return (N) Preconditions.checkNotNull(node, "cannot build Node for clientType="+clientType);
	}

	protected abstract Setting<Boolean> getRecordCallsites();

}
