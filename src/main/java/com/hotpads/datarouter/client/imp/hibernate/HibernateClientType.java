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
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

@Singleton
public class HibernateClientType<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
implements ClientType<PK,D,F>{
	
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
	public Node<PK,D> createNode(NodeParams<PK,D,F> nodeParams){
		if(nodeParams.getFielderClass() == null){
			return new HibernateNode<PK,D,F>(nodeParams);
		}else{
			return new JdbcNode<PK,D,F>(nodeParams);
		}
	}
	
}