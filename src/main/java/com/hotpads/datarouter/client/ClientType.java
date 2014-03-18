package com.hotpads.datarouter.client;

import java.util.List;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.DataRouterContext;

public interface ClientType{

	@Deprecated//should not reserve names
	String getName();
	
	ClientFactory createClientFactory(DataRouterContext drContext, String clientName, 
			List<PhysicalNode<?,?>> physicalNodes);
	
	Node<?,?> createNode(NodeParams<?,?,?> nodeParams);
}
