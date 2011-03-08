package com.hotpads.datarouter.node.factory;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseNode;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class NodeFactory{
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>,
			F extends DatabeanFielder<PK,D>,N extends Node<PK,D>> 
	N create(
			String clientName, 
			Class<D> databeanClass, 
			Class<F> fielderClass,
			DataRouter router){
		
		ClientType clientType = router.getClientOptions().getClientType(clientName);
		
		Node<PK,D> node = null;
		if(ClientType.hibernate==clientType){
			node = new HibernateNode<PK,D,F>(databeanClass, fielderClass, router, clientName);
		}else if(ClientType.hbase==clientType){
			node = new HBaseNode<PK,D,F>(databeanClass, fielderClass, router, clientName);
		}
		
		if(node==null){
			throw new IllegalArgumentException("cannot find Node for clientType="+clientType);
		}
		@SuppressWarnings("unchecked")
		N typedNode = (N)node;
		return typedNode;
	}
	
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>,N extends Node<PK,D>> 
	N create(
			String clientName, 
			Class<D> databeanClass, 
			DataRouter router){
		return create(clientName, databeanClass, null, router);
	}
	
	
	
	//specify tableName and entityName
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>,
			F extends DatabeanFielder<PK,D>,N extends Node<PK,D>> 
	N create(
			String clientName, 
			String tableName,
			String entityName,
			Class<D> databeanClass, 
			DataRouter router){
		
		ClientType clientType = router.getClientOptions().getClientType(clientName);
		
		Node<PK,D> node = null;
		if(ClientType.hibernate==clientType){
			node = new HibernateNode<PK,D,F>(databeanClass, null, router, clientName, tableName, entityName);
		}else if(ClientType.hbase==clientType){
			node = new HBaseNode<PK,D,F>(databeanClass, null, router, clientName, tableName, entityName);
		}
		
		if(node==null){
			throw new IllegalArgumentException("cannot find Node for clientType="+clientType);
		}
		@SuppressWarnings("unchecked")
		N typedNode = (N)node;
		return typedNode;
	}
	
}
