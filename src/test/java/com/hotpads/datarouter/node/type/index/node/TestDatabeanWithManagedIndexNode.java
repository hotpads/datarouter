package com.hotpads.datarouter.node.type.index.node;

import com.hotpads.datarouter.node.factory.IndexingNodeFactory;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByB;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByB.TestDatabeanWithManagedIndexByBFielder;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByC;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByC.TestDatabeanWithManagedIndexByCFielder;
import com.hotpads.datarouter.routing.DataRouter;

public class TestDatabeanWithManagedIndexNode extends TestDatabeanWithIndexNode{
	
	public TestDatabeanWithManagedIndexNode(DataRouter router){
		super(router);
		
		byB = backingMapNode.registerManaged(IndexingNodeFactory.newManagedUnique(router, backingMapNode,
				TestDatabeanWithManagedIndexByBFielder.class, TestDatabeanWithManagedIndexByB.class, false));
		byC = backingMapNode.registerManaged(IndexingNodeFactory.newManagedMulti(router, backingMapNode,
				TestDatabeanWithManagedIndexByCFielder.class, TestDatabeanWithManagedIndexByC.class, false));
	}

	@Override
	protected String getTableName(){
		return "TestDatabeanWithManagedIndex";
	}
	
}
