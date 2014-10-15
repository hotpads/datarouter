package com.hotpads.datarouter.node.type.index.node;

import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedMultiIndexNode;
import com.hotpads.datarouter.node.type.index.ManagedUniqueIndexNode;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByB;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByBKey;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByC;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByCKey;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanKey;
import com.hotpads.datarouter.test.TestDatabeanFielder;

public abstract class TestDatabeanWithIndexNode{

	protected PhysicalMapStorageNode<TestDatabeanKey, TestDatabean> backingMapNode;
	
	public MapStorageNode<TestDatabeanKey, TestDatabean> mainNode;
	
	public ManagedUniqueIndexNode<TestDatabeanKey, TestDatabean, 
	TestDatabeanWithManagedIndexByBKey, TestDatabeanWithManagedIndexByB> byB;
	public ManagedMultiIndexNode<TestDatabeanKey, TestDatabean, 
	TestDatabeanWithManagedIndexByCKey, TestDatabeanWithManagedIndexByC> byC;
	
	public TestDatabeanWithIndexNode(DataRouter router){
		backingMapNode = BaseDataRouter.cast(router.register(NodeFactory.create(DRTestConstants.CLIENT_drTestJdbc0,
				getTableName(), getTableName(), TestDatabean.class,
				TestDatabeanFielder.class, router)));
		mainNode = BaseDataRouter.cast(backingMapNode);
	}
	
	protected abstract String getTableName();
	
}
