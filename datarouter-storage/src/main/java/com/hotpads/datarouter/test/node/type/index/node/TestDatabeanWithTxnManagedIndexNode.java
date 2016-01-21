package com.hotpads.datarouter.test.node.type.index.node;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.IndexingNodeFactory;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByBar;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByBar.TestDatabeanWithManagedIndexByBFielder;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByBaz;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByBaz.TestDatabeanWithManagedIndexByCFielder;

public class TestDatabeanWithTxnManagedIndexNode extends TestDatabeanWithIndexNode {

	public TestDatabeanWithTxnManagedIndexNode(NodeFactory nodeFactory, Router router, ClientId clientId){
		super(nodeFactory, router, clientId);

		byB = backingMapNode.registerManaged(IndexingNodeFactory.newManagedUnique(router, backingMapNode,
				TestDatabeanWithManagedIndexByBFielder.class, TestDatabeanWithManagedIndexByBar.class, true));
		byC = backingMapNode.registerManaged(IndexingNodeFactory.newManagedMulti(router, backingMapNode,
				TestDatabeanWithManagedIndexByCFielder.class, TestDatabeanWithManagedIndexByBaz.class, true));
	}

	@Override
	protected String getTableName(){
		return "TestDatabeanWithTxnManagedIndex";
	}

}
