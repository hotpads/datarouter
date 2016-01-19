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

	@SuppressWarnings("unchecked")
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

	//minimum required fields
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,N extends Node<PK,D>>
	N create(
			ClientId clientId,
			Class<D> databeanClass,
			Router router,
			boolean addAdapter){
		return create(clientId, databeanClass, null, null, router, addAdapter);
	}

	// +fielderClass
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,N extends Node<PK,D>>
	N create(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier, Router router,
			boolean addAdapter){
		return create(clientId, databeanSupplier, fielderSupplier, null, router, addAdapter);
	}

	// +fielderClass
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

	// +fielderClass +schemaVersion
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>,N extends Node<PK,D>>
	N create(ClientId clientId, Supplier<D> databeanSupplier, Supplier<F> fielderSupplier, Integer schemaVersion,
			Router router, boolean addAdapter){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<PK,D,F>(router, databeanSupplier)
				.withClientId(clientId)
				.withFielder(fielderSupplier)
				.withSchemaVersion(schemaVersion)
				.withDiagnostics(getRecordCallsites());
		return create(paramsBuilder.build(), addAdapter);
	}

	// +fielderClass +schemaVersion
	public <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,N extends Node<PK,D>>
	N create(
			ClientId clientId,
			Class<D> databeanClass,
			Class<F> fielderClass,
			Integer schemaVersion,
			Router router,
			boolean addAdapter){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withClientId(clientId)
				.withFielder(fielderClass)
				.withSchemaVersion(schemaVersion)
				.withDiagnostics(getRecordCallsites());
		return create(paramsBuilder.build(), addAdapter);
	}


	/************ include tableName **********************/

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>>
	N create(//specify tableName and entityName
			ClientId clientId,
			String tableName,
			String entityName,
			Class<D> databeanClass,
			Class<F> fielderClass,
			Router router,
			boolean addAdapter){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withClientId(clientId)
				.withFielder(fielderClass)
				.withHibernateTableName(tableName, entityName)
				.withDiagnostics(getRecordCallsites());
		return create(paramsBuilder.build(), addAdapter);
	}


	/***************** entity ***************************/

	@SuppressWarnings("unchecked")
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
//			String parentName,
//			Class<EK> entityKeyClass,//TODO can we do without this?  i couldn't figure out how
//			Class<? extends EntityPartitioner<EK>> entityPartitionerClass,
			Class<D> databeanClass,
			Class<F> fielderClass,
//			Class<E> entityClass,
//			String entityName,
			String entityNodePrefix
			){
//		EntityNodeParams<EK,E> entityNodeParams = new EntityNodeParams<EK,E>(null, entityKeyClass, entityClass,
//				entityPartitionerClass, entityName);
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withClientId(clientId)
				.withParentName(entityNodeParams.getNodeName())
				.withFielder(fielderClass)
				.withEntity(entityNodeParams.getEntityTableName(), entityNodePrefix)
				.withDiagnostics(getRecordCallsites());
		NodeParams<PK,D,F> nodeParams = paramsBuilder.build();
//		return create(paramsBuilder.build());
		ClientType clientType = clients.getClientTypeInstance(clientId.getName());
		Preconditions.checkNotNull(clientType, "clientType not found for clientName:"+clientId.getName());
		Node<PK,D> node = clientType.createSubEntityNode(entityNodeParams, nodeParams);
		return (N) Preconditions.checkNotNull(node, "cannot build Node for clientType="+clientType);
	}


	/*************** baseDatabeanClass ********************/

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>>
	N createWithBaseDatabeanClass(//3 args
			ClientId clientId,
			Class<D> databeanClass,
			Class<? super D> baseDatabeanClass,
			Router router){
		NodeParamsBuilder<PK,D,?> paramsBuilder = new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withClientId(clientId)
				.withBaseDatabean(baseDatabeanClass)
				.withDiagnostics(getRecordCallsites());
		return create(paramsBuilder.build(), true);
	}


	/***************** private **************************/

	private Cached<Boolean> getRecordCallsites(){
		return datarouterSettings.getRecordCallsites();
	}
}
