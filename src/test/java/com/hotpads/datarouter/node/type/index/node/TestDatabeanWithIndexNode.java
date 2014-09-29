package com.hotpads.datarouter.node.type.index.node;

import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedMultiIndexNode;
import com.hotpads.datarouter.node.type.index.ManagedUniqueIndexNode;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndex;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndex.TestDatabeanWithManagedIndexFielder;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByB;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByBKey;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByC;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByCKey;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexKey;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.test.DRTestConstants;

public abstract class TestDatabeanWithIndexNode{

	protected PhysicalMapStorageNode<TestDatabeanWithManagedIndexKey, TestDatabeanWithManagedIndex> backingMapNode;
	
	public MapStorageNode<TestDatabeanWithManagedIndexKey, TestDatabeanWithManagedIndex> mainNode;
	
	public ManagedUniqueIndexNode<TestDatabeanWithManagedIndexKey, TestDatabeanWithManagedIndex, 
	TestDatabeanWithManagedIndexByBKey, TestDatabeanWithManagedIndexByB> byB;
	public ManagedMultiIndexNode<TestDatabeanWithManagedIndexKey, TestDatabeanWithManagedIndex, 
	TestDatabeanWithManagedIndexByCKey, TestDatabeanWithManagedIndexByC> byC;
	
	public TestDatabeanWithIndexNode(DataRouter router){
		backingMapNode = BaseDataRouter.cast(router.register(NodeFactory.create(DRTestConstants.CLIENT_drTestJdbc0,
				getTableName(), getTableName(), TestDatabeanWithManagedIndex.class,
				TestDatabeanWithManagedIndexFielder.class, router)));
		mainNode = BaseDataRouter.cast(backingMapNode);
	}
	
	protected abstract String getTableName();
	
}
