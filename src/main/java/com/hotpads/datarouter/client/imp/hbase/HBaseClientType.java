package com.hotpads.datarouter.client.imp.hbase;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Singleton;

import com.hotpads.datarouter.client.ClientFactory;
import com.hotpads.datarouter.client.DClientType;
import com.hotpads.datarouter.client.imp.hbase.factory.HBaseSimpleClientFactory;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;

@Singleton
public class HBaseClientType implements DClientType{
	
	public static final String NAME = "hbase";
	
	@Override
	public String getName(){
		return NAME;
	}
	
	@Override
	public ClientFactory createClientFactory(DataRouterContext drContext, String clientName,
			List<PhysicalNode<?,?>> physicalNodes, ExecutorService executorService){
		// if(USE_RECONNECTING_HBASE_CLIENT){
		// return new HBaseDynamicClientFactory(router, clientName,
		// configFileLocation, executorService);
		// }else{
		return new HBaseSimpleClientFactory(drContext, clientName, executorService);
		// }
	}

}
