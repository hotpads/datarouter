package com.hotpads.datarouter.test.node.basic.sorted;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DatarouterTestClientIds;

public class MemoryIndexedSortedNodeIntegrationTests extends BaseIndexedNodeIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterTestClientIds.CLIENT_drTestMemory, false);
	}

	@AfterClass
	public void afterClass(){
		postTestTests();
		datarouter.shutdown();
	}

	@Override
	protected void testIgnoreNull(){
		// Skip because feature is not yet implemented
	}

}
