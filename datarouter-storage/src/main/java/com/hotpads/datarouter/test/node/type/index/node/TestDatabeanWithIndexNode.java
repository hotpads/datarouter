package com.hotpads.datarouter.test.node.type.index.node;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.IndexedMapStorage.PhysicalIndexedMapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.type.index.MultiIndexNode;
import com.hotpads.datarouter.node.type.index.UniqueIndexNode;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanFielder;
import com.hotpads.datarouter.test.TestDatabeanKey;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByBar;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByBarKey;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByBaz;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByBazKey;

public abstract class TestDatabeanWithIndexNode{

	protected PhysicalIndexedMapStorageNode<TestDatabeanKey, TestDatabean> backingMapNode;

	public MapStorageNode<TestDatabeanKey, TestDatabean> mainNode;

	public UniqueIndexNode<TestDatabeanKey,
							TestDatabean,
							TestDatabeanWithManagedIndexByBarKey,
							TestDatabeanWithManagedIndexByBar> byB;
	public MultiIndexNode<TestDatabeanKey,
							TestDatabean,
							TestDatabeanWithManagedIndexByBazKey,
							TestDatabeanWithManagedIndexByBaz> byC;

	public TestDatabeanWithIndexNode(NodeFactory nodeFactory, Router router, ClientId clientId){
		backingMapNode = router.register(nodeFactory.create(clientId, getTableName(), getTableName(),
				TestDatabean.class, TestDatabeanFielder.class, router, false));
		mainNode = backingMapNode;
	}

	protected abstract String getTableName();

}
