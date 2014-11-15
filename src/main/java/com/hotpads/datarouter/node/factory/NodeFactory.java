package com.hotpads.datarouter.node.factory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.setting.DatarouterSettings;

@Singleton
public class NodeFactory{
	private static final Logger logger = LoggerFactory.getLogger(NodeFactory.class);
	
	private final DatarouterSettings drSettings;
	
	
	@Inject
	private NodeFactory(@Nullable DatarouterSettings drSettings){
		this.drSettings = drSettings;
	}


	/********************* pass any params *****************/
	
	public static <
			PK extends PrimaryKey<PK>,
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
	
	
	/*************** simple helpers *********************/
	
	//minimum required fields
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,N extends Node<PK,D>> 
	N create(//3 args
			String clientName, 
			Class<D> databeanClass, 
			DataRouter router,
			boolean addAdapter){
		return create(clientName, databeanClass, null, null, router, addAdapter);
	}
	
	// +fielderClass
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,N extends Node<PK,D>> 
	N create(//4 args
			String clientName, 
			Class<D> databeanClass, 
			Class<F> fielderClass,
			DataRouter router,
			boolean addAdapter){
		return create(clientName, databeanClass, fielderClass, null, router, addAdapter);
	}
	
	// +fielderClass +schemaVersion
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,N extends Node<PK,D>> 
	N create(//5 args
			String clientName, 
			Class<D> databeanClass, 
			Class<F> fielderClass,
			Integer schemaVersion,
			DataRouter router,
			boolean addAdapter){				
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withClientName(clientName)
				.withFielder(fielderClass)
				.withSchemaVersion(schemaVersion);
		return create(paramsBuilder.build(), addAdapter);
	}
	
	
	/************ include tableName **********************/
	
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>> 
	N create(
			String clientName, 
			String tableName,
			String entityName,
			Class<D> databeanClass, 
			DataRouter router){
		return create(clientName, tableName, entityName, databeanClass, null, router, true);
	}
	
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>> 
	N create(//specify tableName and entityName
			String clientName, 
			String tableName,
			String entityName,
			Class<D> databeanClass, 
			Class<F> fielderClass,
			DataRouter router,
			boolean addAdapter){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withClientName(clientName)
				.withFielder(fielderClass)
				.withHibernateTableName(tableName, entityName);
		return create(paramsBuilder.build(), addAdapter);
	}
	
	
	/***************** entity ***************************/

	public <EK extends EntityKey<EK>,
			E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>> 
	N subEntityNode(//specify entityName and entityNodePrefix
			DataRouter router,
			EntityNodeParams<EK,E> entityNodeParams,
			String clientName,
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
				.withClientName(clientName)
				.withParentName(entityNodeParams.getNodeName())
				.withFielder(fielderClass)
				.withEntity(entityNodeParams.getEntityTableName(), entityNodePrefix);
		NodeParams<PK,D,F> nodeParams = paramsBuilder.build();
//		return create(paramsBuilder.build());
		ClientType clientType = nodeParams.getRouter().getClientOptions().getClientTypeInstance(clientName);
		Preconditions.checkNotNull(clientType, "clientType not found for clientName:"+clientName);
		N node = (N)clientType.createSubEntityNode(entityNodeParams, nodeParams);
		return Preconditions.checkNotNull(node, "cannot build Node for clientType="+clientType);
	}	
	
	
	/*************** baseDatabeanClass ********************/
	
	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			N extends Node<PK,D>>
	N createWithBaseDatabeanClass(//3 args
			String clientName, 
			Class<D> databeanClass, 
			Class<? super D> baseDatabeanClass,
			DataRouter router){
		NodeParamsBuilder<PK,D,?> paramsBuilder = new NodeParamsBuilder(router, databeanClass)
				.withClientName(clientName)
				.withBaseDatabean(baseDatabeanClass)
				.withDiagnostics(drSettings.getRecordCallsites());
		return create(paramsBuilder.build(), true);
	}
}
