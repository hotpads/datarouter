package com.hotpads.datarouter.node.type.index.databean;

import com.hotpads.datarouter.node.factory.IndexingNodeFactory;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.index.ManagedMultiIndexNode;
import com.hotpads.datarouter.node.type.index.ManagedUniqueIndexNode;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndex.TestDatabeanWithManagedIndexFielder;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByB.TestDatabeanWithManagedIndexByBFielder;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByC.TestDatabeanWithManagedIndexByCFielder;
import com.hotpads.datarouter.routing.BaseDataRouter;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.test.DRTestConstants;

public class TestDatabeanWithManagedIndexNode{

	private PhysicalMapStorageNode<TestDatabeanWithManagedIndexKey, TestDatabeanWithManagedIndex> backingMapNode;
	
	public MapStorageNode<TestDatabeanWithManagedIndexKey, TestDatabeanWithManagedIndex> mainNode;
	
	public ManagedUniqueIndexNode<TestDatabeanWithManagedIndexKey, TestDatabeanWithManagedIndex, 
	TestDatabeanWithManagedIndexByBKey, TestDatabeanWithManagedIndexByB> byB;
	public ManagedMultiIndexNode<TestDatabeanWithManagedIndexKey, TestDatabeanWithManagedIndex, 
	TestDatabeanWithManagedIndexByCKey, TestDatabeanWithManagedIndexByC> byC;
	
	
	public TestDatabeanWithManagedIndexNode(DataRouter router){
		backingMapNode = BaseDataRouter.cast(router.register(NodeFactory.create(DRTestConstants.CLIENT_drTestJdbc0,
				TestDatabeanWithManagedIndex.class, TestDatabeanWithManagedIndexFielder.class, router)));
		mainNode = BaseDataRouter.cast(backingMapNode);
		byB = IndexingNodeFactory.newManagedUnique(router, backingMapNode, TestDatabeanWithManagedIndexByBFielder.class,
				TestDatabeanWithManagedIndexByB.class);
		byC = IndexingNodeFactory.newManagedMulti(router, backingMapNode, TestDatabeanWithManagedIndexByCFielder.class,
				TestDatabeanWithManagedIndexByC.class);
	}
	
}
