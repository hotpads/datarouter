package com.hotpads.datarouter.node.factory;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.entity.Entity;
import com.hotpads.datarouter.storage.key.entity.EntityKey;
import com.hotpads.datarouter.storage.key.primary.EntityPrimaryKey;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class NodeFactory{
	private static Logger logger = Logger.getLogger(NodeFactory.class);
	
	/********************* pass any params *****************/
	
	public static <
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,
			N extends Node<PK,D>> 
	N create(NodeParams<PK,D,F> params){
		String clientName = params.getClientName();
		ClientType clientType = params.getRouter().getClientOptions().getClientTypeInstance(clientName);
		Preconditions.checkNotNull(clientType, "clientType not found for clientName:"+clientName);
		N node = (N)clientType.createNode(params);
		return Preconditions.checkNotNull(node, "cannot build Node for clientType="+clientType);
	}
	
	
	/*************** simple helpers *********************/
	
	//minimum required fields
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,N extends Node<PK,D>> 
	N create(//3 args
			String clientName, 
			Class<D> databeanClass, 
			DataRouter router){
		return create(clientName, databeanClass, null, null, router);
	}
	
	// +fielderClass
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,N extends Node<PK,D>> 
	N create(//4 args
			String clientName, 
			Class<D> databeanClass, 
			Class<F> fielderClass,
			DataRouter router){
		return create(clientName, databeanClass, fielderClass, null, router);
	}
	
	// +fielderClass +schemaVersion
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,N extends Node<PK,D>> 
	N create(//5 args
			String clientName, 
			Class<D> databeanClass, 
			Class<F> fielderClass,
			Integer schemaVersion,
			DataRouter router){				
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withClientName(clientName)
				.withFielder(fielderClass)
				.withSchemaVersion(schemaVersion);
		return create(paramsBuilder.build());
	}
	
	
	/************ include tableName **********************/
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,N extends Node<PK,D>> 
	N create(
			String clientName, 
			String tableName,
			String entityName,
			Class<D> databeanClass, 
			DataRouter router){
		return create(clientName, tableName, entityName, databeanClass, null, router);
	}
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,N extends Node<PK,D>> 
	N create(//specify tableName and entityName
			String clientName, 
			String tableName,
			String entityName,
			Class<D> databeanClass, 
			Class<F> fielderClass,
			DataRouter router){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withClientName(clientName)
				.withFielder(fielderClass)
				.withHibernateTableName(tableName, entityName);
		return create(paramsBuilder.build());
	}
	
	
	/***************** entity ***************************/

	public static <EK extends EntityKey<EK>,E extends Entity<EK>,
			PK extends EntityPrimaryKey<EK,PK>,D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>,N extends Node<PK,D>> 
	N subEntityNode(//specify entityName and entityNodePrefix
			DataRouter router,
			String clientName,
			String parentName,
			Class<EK> entityKeyClass,//TODO can we do without this?  i couldn't figure out how
			Class<D> databeanClass, 
			Class<F> fielderClass,
			Class<E> entityClass,
			String entityName,
			String entityNodePrefix
			){
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withClientName(clientName)
				.withParentName(parentName)
				.withFielder(fielderClass)
				.withEntity(entityClass, entityName, entityNodePrefix);
		return create(paramsBuilder.build());
	}	
	
	
	/*************** baseDatabeanClass ********************/
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,N extends Node<PK,D>>
	N createWithBaseDatabeanClass(//3 args
			String clientName, 
			Class<D> databeanClass, 
			Class<? super D> baseDatabeanClass,
			DataRouter router){
		NodeParamsBuilder<PK,D,?> paramsBuilder = new NodeParamsBuilder(router, databeanClass)
				.withClientName(clientName)
				.withBaseDatabean(baseDatabeanClass);
		return create(paramsBuilder.build());
	}
}
