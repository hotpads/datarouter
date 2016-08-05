package com.hotpads.datarouter.node.factory;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.DatarouterClients;
import com.hotpads.datarouter.config.DatarouterSettings;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.cache.Cached;
import com.hotpads.util.core.java.ReflectionTool;

@Singleton
public class NodeFactory{

	private final DatarouterClients clients;
	private final DatarouterSettings datarouterSettings;

	@Inject
	private NodeFactory(DatarouterClients clients, DatarouterSettings datarouterSettings){
		this.clients = clients;
		this.datarouterSettings = datarouterSettings;
	}

	/********************* pass any params *****************/

	private <PK extends PrimaryKey<PK>,
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

	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,N extends Node<PK,D>>
	N create(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier, Router router,
			boolean addAdapter){
		return create(clientId, databeanSupplier, fielderSupplier, null, router, addAdapter);
	}

	/**
	 * @deprecated use {@link #create(ClientId, Supplier, Supplier, Router, boolean)}
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
		return create(clientId, databeanClass, fielderClass, null, router, addAdapter);
	}

	// +schemaVersion
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,N extends Node<PK,D>>
	N create(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier, Integer schemaVersion,
			Router router, boolean addAdapter){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<>(router, databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withSchemaVersion(schemaVersion)
				.withDiagnostics(getRecordCallsites());
		return create(paramsBuilder.build(), addAdapter);
	}

	// +schemaVersion
	/**
	 * @deprecated use {@link #create(ClientId, Supplier, Supplier, Integer, Router, boolean)}
	 */
	@Deprecated
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,N extends Node<PK,D>>
	N create(ClientId clientId, Class<D> databeanClass, Class<F> fielderClass, Integer schemaVersion, Router router,
			boolean addAdapter){
		return create(clientId, ReflectionTool.supplier(databeanClass), ReflectionTool.supplier(fielderClass),
				schemaVersion, router, addAdapter);
	}

	/************ include tableName **********************/

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>>
	N create(//specify tableName and entityName
			ClientId clientId,
			String tableName,
			Supplier<D> databeanSupplier,
			Supplier<F> fielderSupplier,
			Router router,
			boolean addAdapter){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<>(router, databeanSupplier, fielderSupplier)
				.withClientId(clientId)
				.withTableName(tableName)
				.withDiagnostics(getRecordCallsites());
		return create(paramsBuilder.build(), addAdapter);
	}

	/**
	 * @deprecated use {@link #create(ClientId, String, String, Supplier, Supplier, Router, boolean)}
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
		return create(clientId, tableName, ReflectionTool.supplier(databeanClass),
				ReflectionTool.supplier(fielderClass), router, addAdapter);
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

	/***************** private **************************/

	private Cached<Boolean> getRecordCallsites(){
		return datarouterSettings.getRecordCallsites();
	}
}
