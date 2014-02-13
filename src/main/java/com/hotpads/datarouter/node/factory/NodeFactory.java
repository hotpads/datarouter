package com.hotpads.datarouter.node.factory;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class NodeFactory{
	static Logger logger = Logger.getLogger(NodeFactory.class);
	
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
		ClientType clientType = router.getClientOptions().getClientTypeInstance(clientName);
		Preconditions.checkNotNull(clientType, "clientType not found for clientName:"+clientName);
		
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<PK,D,F>(router, clientName, databeanClass)
				.withFielder(fielderClass)
				.withSchemaVersion(schemaVersion);
		N node = (N)clientType.createNode(paramsBuilder.build());
		return Preconditions.checkNotNull(node, "cannot build Node for clientType="+clientType);
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
		ClientType clientType = router.getClientOptions().getClientTypeInstance(clientName);
		Preconditions.checkNotNull(clientType, "clientType not found for clientName:"+clientName);
		
		NodeParamsBuilder<PK,D,F> paramsBuilder = new NodeParamsBuilder<PK,D,F>(router, clientName, databeanClass)
				.withFielder(fielderClass)
				.withHibernateTableName(tableName, entityName);
		N node = (N)clientType.createNode(paramsBuilder.build());
		return Preconditions.checkNotNull(node, "cannot build Node for clientType="+clientType);
	}
	
	
	/*************** baseDatabeanClass ********************/
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK,D>,N extends Node<PK,D>>
	N createWithBaseDatabeanClass(//3 args
			String clientName, 
			Class<D> databeanClass, 
			Class<? super D> baseDatabeanClass,
			DataRouter router){
		ClientType clientType = router.getClientOptions().getClientTypeInstance(clientName);
		Preconditions.checkNotNull(clientType, "clientType not found for clientName:"+clientName);
		
		NodeParamsBuilder<PK,D,?> paramsBuilder = new NodeParamsBuilder(router, clientName, databeanClass)
				.withBaseDatabean(baseDatabeanClass);
		N node = (N)clientType.createNode(paramsBuilder.build());
		return Preconditions.checkNotNull(node, "cannot build Node for clientType="+clientType);
	}
}
