package com.hotpads.datarouter.client.imp.hibernate;

import java.util.List;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hibernate.factory.HibernateSimpleClientFactory;
import com.hotpads.datarouter.client.imp.hibernate.node.HibernateNode;
import com.hotpads.datarouter.client.imp.jdbc.node.JdbcNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.entity.EntityNodeParams;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;

@Singleton
public class HibernateClientType
implements ClientType{
	private static final Logger logger = LoggerFactory.getLogger(HibernateClientType.class);
	
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
			Node<?,?> node = new HibernateNode(nodeParams);
			logger.warn("creating HibernateNode "+node);
			return node;
		}else{
			return new JdbcNode(nodeParams);
		}
	}
	
	//ignore the entityNodeParams
	@Override
	public Node<?,?> createSubEntityNode(EntityNodeParams<?,?> entityNodeParams, NodeParams<?,?,?> nodeParams){
		return createNode(nodeParams);
	}
	
}
