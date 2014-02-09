package com.hotpads.datarouter.client.imp.jdbc;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.DClientType;
import com.hotpads.datarouter.client.imp.jdbc.factory.JdbcSimpleClientFactory;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;

@Singleton
public class JdbcClientType implements DClientType{
	
	public static final String NAME = "jdbc";
	
	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public ClientFactory createClientFactory(DataRouterContext drContext, String clientName,
			List<PhysicalNode<?,?>> physicalNodes, ExecutorService executorService){
		return new JdbcSimpleClientFactory(drContext, clientName, executorService);
	}
	
}
