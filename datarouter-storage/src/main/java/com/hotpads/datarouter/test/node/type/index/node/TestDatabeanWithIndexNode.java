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
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByB;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByBKey;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByC;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByCKey;

public abstract class TestDatabeanWithIndexNode{

	protected PhysicalIndexedMapStorageNode<TestDatabeanKey, TestDatabean> backingMapNode;

	public MapStorageNode<TestDatabeanKey, TestDatabean> mainNode;

	public UniqueIndexNode<TestDatabeanKey,
							TestDatabean,
							TestDatabeanWithManagedIndexByBKey,
							TestDatabeanWithManagedIndexByB> byB;
	public MultiIndexNode<TestDatabeanKey,
							TestDatabean,
							TestDatabeanWithManagedIndexByCKey,
							TestDatabeanWithManagedIndexByC> byC;

	public TestDatabeanWithIndexNode(NodeFactory nodeFactory, Router router, ClientId clientId){
		backingMapNode = router.register(nodeFactory.create(clientId, getTableName(), getTableName(),
				TestDatabean.class, TestDatabeanFielder.class, router, false));
		mainNode = backingMapNode;
	}

	protected abstract String getTableName();

}
