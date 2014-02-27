package com.hotpads.datarouter.client.imp.hibernate;

import java.util.List;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hibernate.factory.HibernateSimpleClientFactory;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;

@Singleton
public class HibernateClientType
implements ClientType{
	
	public static final String NAME = "hibernate";
	
	public static final HibernateClientType INSTANCE = new HibernateClientType();
	
	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public ClientFactory createClientFactory(DataRouterContext drContext, String clientName,
			List<PhysicalNode<?,?>> physicalNodes){
		return new HibernateSimpleClientFactory(drContext, clientName); 
	}
	
	@Override
	public Node<?,?> createNode(NodeParams<?,?,?> nodeParams){
		if(nodeParams.getFielderClass() == null){
			return new HibernateNode(nodeParams);
		}else{
			return new JdbcNode(nodeParams);
		}
	}
	
}
