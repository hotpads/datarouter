package com.hotpads.datarouter.node;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hbase.HBaseNode;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class NodeFactory{
	
	@SuppressWarnings("unchecked")
	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>,N extends Node<PK,D>> 
	N cast(Node<PK,D> in){
		return (N)in;
	}

	public static <PK extends PrimaryKey<PK>,D extends Databean<PK>,N extends Node<PK,D>> 
	N create(
			String clientName, 
			Class<D> databeanClass, 
			DataRouter router){
		
		ClientType clientType = router.getClientOptions().getClientType(clientName);
		
		Node<PK,D> node = null;
		if(ClientType.hibernate==clientType){
			node = new HibernateNode<PK,D>(databeanClass, router, clientName);
		}else if(ClientType.hbase==clientType){
			node = new HBaseNode<PK,D>(databeanClass, router, clientName);
		}
		
		if(node==null){
			throw new IllegalArgumentException("cannot find MapStorageNode for clientType="+clientType);
		}
		@SuppressWarnings("unchecked")
		N typedNode = (N)node;
		return typedNode;
	}
	
}
