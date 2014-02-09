package com.hotpads.datarouter.client;

import java.util.List;
import java.util.concurrent.ExecutorService;

import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;

public interface DClientType{

	@Deprecated//should not reserve names
	String getName();
	
	ClientFactory createClientFactory(DataRouterContext drContext, String clientName, 
			List<PhysicalNode<?,?>> physicalNodes, ExecutorService executorService);
	
}
